(ns tic-tac-toe.api
  (:require [tailrecursion.castra :refer [defrpc]]
            [tic-tac-toe.game :as game]))


(def matches (atom {}))

(defn rand-id []
  (clojure.string/join
   ""
   (repeatedly 10 #(rand-nth "1234567890qwertyuiopasdfghjklzxcvbnmQWERTYUIOPASDFGHJKLZXCVBNM"))))

(defn days []
  (->  (System/currentTimeMillis) (quot 1000) (quot 60) (quot 60) (quot 24)))

(future
  (while true
    (try
      (Thread/sleep (* 1000 60 60 24 2)) ; 2days
      (swap! matches (fn [m]
                       (->> m
                            (filter #(< (- (days) (:last-touch (second %))) 3))
                            (into {}))))
      (catch Exception e
        (println (str "caught exception: " (.getMessage e)))))))

(defn new-match-data [width height]
  {:game-data
   {:position (vec (repeat height (vec (repeat width nil))))
    :turn :x}
   :players []
   :last-touch (days)})

(defn get-side [players player-id]
  (case (.indexOf players player-id)
                 0 :x
                 1 :0
                 :view))

(defn result-answer [game-id player-id]
  (merge (get-in @matches [game-id :game-data])
         {:num (get-side (get-in @matches [game-id :players]) player-id)}))

(defrpc new-game [width height]
  {:rpc/pre [(> width 4)
             (> height 4)]}
  (let [game-id (rand-id)]
    (swap! matches assoc game-id (new-match-data width height))
    {:match game-id}))

(defrpc connect-to-game [game-id]
  {:rpc/pre [(get @matches game-id)]}
  (if (< (count (get-in @matches [game-id :players])) 2)
    (let [player-id (rand-id)]
      (swap! matches update-in [game-id :players] #(conj % player-id))
      {:player-id player-id
       :match game-id})
    {:player-id "view"
     :match game-id}))

(defrpc get-state [game-id player-id]
  {:rpc/pre [(get @matches game-id)]}
  (result-answer game-id player-id))

(defrpc move [game-id player-id move-cell]
  {:rpc/pre [(get @matches game-id)]}
  (let [players (get-in @matches [game-id :players])
        turn (get-in @matches [game-id :game-data :turn])
        position (get-in @matches [game-id :game-data :position])]
    (if-not (and (some #{player-id} players)  ;its valid player
                 (= (get-side players player-id) ; its player's turn to move
                    (get-in @matches [game-id :game-data :turn]))
                 (game/possible-move? position move-cell))
      (throw (Exception. "Impossible move"))
      (do
        (swap! matches
                 (fn [m]
                   (-> m
                       (assoc-in [game-id :last-touch] (days))
                       (assoc-in [game-id :game-data :last-move] move-cell)
                       (update-in [game-id :game-data] game/player-move move-cell))))
        (result-answer game-id player-id)))))

