(ns genetic.core
  (:use [seesaw core chooser graphics]
	[genetic genetic])
  (:import [java.io File]
	   [java.awt RenderingHints]
	   [java.awt.image BufferedImage]
	   [javax.imageio ImageIO]))

(def target-image
     (atom
      (ImageIO/read
       (File. "images/starter-image.jpg"))))

(def evolved-image (atom @target-image))

(defn resize-image
  [img]
  (let [scaled-image (buffered-image 500 500)
	g2d (.createGraphics scaled-image)]
    (.setRenderingHint g2d
		       RenderingHints/KEY_INTERPOLATION
		       RenderingHints/VALUE_INTERPOLATION_BILINEAR)
    (.drawImage g2d img 0 0 500 500 nil)
    (.dispose g2d)
    scaled-image))

(def target-action
     (action
      :handler (fn [e]
		 (choose-file
		  :type :open
		  :success-fn (fn [fc f]
				(swap! target-image (fn [u]
						      (resize-image
						       (ImageIO/read f))))
				(repaint!
				 (select (to-root e) [:#w])))
		  :filters [["Images" ["png" "jpeg" "jpg"]]]))
      :name "Choose Picture"
      :key  "menu T"
      :tip  "Change the picture to evolve towards a new target."))

(def evolve-action
     (action
      :handler (fn [e] nil)
      :name "Start Evolving"
      :key  "menu E"
      :tip  "Evolve towards the image."))

(defn paint-target-image [c g]
  (.drawImage g @target-image 0 0 nil))

(defn paint-evolved-image [c g]
  (.drawImage g @evolved-image 0 0 nil))


(defn -main [& args]
  (native!)
  (invoke-later 
   (-> (frame :title "Genetic Picture"
	      :menubar (menubar
			:items [(menu :text "File" :items [target-action
							   evolve-action])])
	      :content (border-panel
			:hgap 5 :vgap 5 :border 5
			:west (canvas :paint paint-target-image
				      :id :w
				      :size [500 :by 500])
			:east (canvas :paint paint-evolved-image
				      :id :e
				      :size [500 :by 500]))
	      :on-close :dispose)
       pack!
       show!)))