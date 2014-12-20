(ns tic-tac-toe.game)

(defn possible-move? [position [i j :as move-cell]]
  (and (< i (count position))
       (< j (count (position 0)))
       (nil? (get-in position move-cell))))

(defn next-turn [game-data]
  (update-in game-data [:turn] #(case %
                                  :x :0
                                  :0 :x)))

(defn sum-cells [cell-1 cell-2]
  (map + cell-1 cell-2))

(defn count-in-delta [position from-cell delta]
  (let [sym (get-in position from-cell)]
    (loop [sym-count 0
           current-cell (sum-cells from-cell delta)]
      (if (= sym (get-in position current-cell))
        (recur (inc sym-count) (sum-cells current-cell delta))
        sym-count))))

(defn count-in-line [position from-cell delta]
  (+ (count-in-delta position from-cell delta)
     (count-in-delta position from-cell (map - delta))))

(defn check-win [{:keys [position] :as game-data} move-cell]
  (if (some #{true} (map #(= % 4)
                         (for [delta [[1 0] [0 1] [-1 1] [-1 -1]]]
                           (count-in-line position move-cell delta))))
    (assoc game-data :win (get-in position move-cell))
    game-data))

(defn player-move [game-data [i j :as move-cell]]
  (-> game-data
      (#(assoc-in % [:position i j] (:turn %)))
      (check-win move-cell)
      next-turn))
