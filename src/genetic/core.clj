(ns genetic.core
  (:use [seesaw core chooser graphics]
	[genetic genetic])
  (:import [java.io File]
	   [java.awt RenderingHints]
	   [java.awt.image BufferedImage]
	   [javax.imageio ImageIO]
	   [java.util.concurrent Executors]))

(def target-image
     (atom
      (ImageIO/read
       (File. "images/starter-image.jpg"))))
(def best-image-since-refresh (atom @target-image))

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

(def change-target-action
     (action
      :handler (fn [e]
		 (choose-file
		  :type :open
		  :success-fn (fn [fc f]
				(swap! target-image (fn [u]
						      (resize-image
						       (ImageIO/read f))))
				(repaint!
				 (select (to-root e) [:#w]))
				(change-evolution-target @target-image))
		  :filters [["Images" ["png" "jpeg" "jpg"]]]))
      :name "Choose Picture"
      :key  "menu T"
      :tip  "Change the picture to evolve towards a new target."))

(def refresh-evolved-image-action
     (action
      :handler (fn [e]
		 (swap! best-image-since-refresh get-best-evolved-image)
		 (repaint!
		  (select (to-root e) [:#e])))
      :name "Refresh Evolved Image"
      :key  "menu R"
      :tip  "Displays the best of the currently evolved generation."))

(defn paint-target-image [c g]
  (.drawImage g @target-image 0 0 nil))

(defn paint-evolved-image [c g]
  (.drawImage g @best-image-since-refresh 0 0 nil))

(defn start-evolution-thread []
  (change-evolution-target @target-image)
  (start-evolving))

(defn -main [& args]
  (native!)
  (.start (Thread. start-evolution-thread))
  (invoke-later 
   (-> (frame :title "Genetic Picture"
	      :menubar (menubar
			:items [(menu :text "File" :items
				      [change-target-action
				       refresh-evolved-image-action])])
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