(ns genetic.genetic
  (:use [seesaw color graphics]))

(defn Color []
  (color (inc (rand-int 255)) (inc (rand-int 255)) (inc (rand-int 255))))

(defn Polygon []
  (apply polygon
	 (repeatedly (+ 3 (rand-int 10))
		     (fn [] [(rand-int 501) (rand-int 501)]))))

(defn paint-image
  [seq]
  (let [img (buffered-image 500 500)
	g2d (.createGraphics img)]
    (doseq [s seq]
      (if (= java.awt.Color (class s))
	(.setColor g2d s)
	(.fill g2d s)))
    (.dispose g2d)
    img))


(defn generate-sequence []
  (for [x (range 100)]
    ((rand-nth [Color Polygon]))))

(defn generate-initial-population []
  (for [x (range 100)]
    (generate-sequence)))



(defn abs [x]
  (if (neg? x)
    (* -1 x)
    x))

(defn image-fitness
  [target evolved]
  (abs (let [distances (for [x (range (.getWidth target))
			     y (range (.getHeight target))]
			 (abs (- (.getRGB target x y) (.getRGB evolved x y))))]
	 (/ (reduce + distances) (count distances)))))