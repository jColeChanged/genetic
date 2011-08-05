(ns genetic.genetic
  (:use [seesaw color graphics]))

(def evolution-target (atom (buffered-image 500 500)))
(defn change-evolution-target [new-target]
  (swap! evolution-target (fn [unused] new-target)))

(def best-evolved-image (atom (buffered-image 500 500)))
(defn get-best-evolved-image [& args]
  @best-evolved-image)

(def done (atom false))

(defn Color []
  (color (inc (rand-int 255)) (inc (rand-int 255)) (inc (rand-int 255)) (rand-int 255)))

(defn Polygon []
  (apply polygon
	 (repeatedly (+ 3 (rand-int 4))
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

(defn generate-sequence [size]
  (for [x (range size)]
    ((rand-nth [Color Polygon]))))

(defn generate-initial-population []
  (for [x (range 10)]
    (generate-sequence 100)))


    

(defn abs [x]
  (if (neg? x)
    (* -1 x)
    x))

(defn image-fitness
  [target evolved-sequence]
  (let [evolved (paint-image evolved-sequence)
  	distances (map #(abs (-  (.getRGB target  (first %) (second %))
  				 (.getRGB evolved (first %) (second %))))
  		       (repeatedly 1000 (fn []
  					 [(rand-int 500) (rand-int 500)])))]
    (/ (reduce + distances)
       (count distances))))
 
(defn evaluate
  [target collection]
  (sort #(< (second %1) (second %2))
	(map (juxt identity (partial image-fitness target))
	     collection)))

(defn mutate
  ([seq] (mutate seq 3))
  ([seq times]
     (if (zero? times) seq
	 (mutate 
	  (assoc (vec seq) (rand-int 100) ((rand-nth [Polygon Color])))
	  (dec times)))))

(defn mutate-collection
  [seq]
  (map
   mutate
   (apply concat
	  (for [x (map first (take 1 seq))]
	    (replicate 10 x)))))

(defn start-evolving []
  (loop [population (evaluate @evolution-target (generate-initial-population))]
    (do
      (swap! best-evolved-image (fn [u] (paint-image (ffirst population))))
      (if @done
	nil
	(recur (concat
		population
		(evaluate @evolution-target (mutate-collection population))))))))