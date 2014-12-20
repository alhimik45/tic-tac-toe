(ns tic-tac-toe.rpc
  (:require-macros
   [tailrecursion.javelin :refer [defc defc= cell=]])
  (:require
   [tailrecursion.javelin]
   [tailrecursion.castra :refer [mkremote]]))

(defc state {:position nil
             :turn :x})
(defc first-state {:position nil
                   :turn :x})

(defc error nil)
(defc loading [])

(cell= (when error (.log js/console (:cause error))))

(defc= match-id (get state :match))
(defc= player-id (get state :player-id))

(def get-state
  (mkremote 'tic-tac-toe.api/get-state state error loading))

(def get-state-first
  (mkremote 'tic-tac-toe.api/get-state first-state error loading))

(def new-game
  (mkremote 'tic-tac-toe.api/new-game state error loading))

(def connect-to-game
  (mkremote 'tic-tac-toe.api/connect-to-game state error loading))

(def move
  (mkremote 'tic-tac-toe.api/move state error loading))

(defn game-loop [game-id player-id]
  (get-state-first game-id player-id)
  (js/setInterval #(get-state game-id player-id) 1000))
