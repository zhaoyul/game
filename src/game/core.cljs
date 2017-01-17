(ns game.core
  (:require [reagent.core :as reagent :refer [atom]]))

(enable-console-print!)

(println "This text is printed from src/game/core.cljs. Go ahead and edit it and see reloading in action.")

;; define your app data so that it doesn't get over-written on reload

(def BOARD_SIZE 9)
(def WINNING_SIZE 5)

(defn init-board
  [board-size]
  (vec (repeat board-size (vec (repeat board-size :blank)))))

(defonce app-state (atom {:text "Hello world!"
                          :board (init-board BOARD_SIZE)
                          :game-status :in-progress}))

(defn win-line? [owner board [x y] [dx dy] n]
  (every? true?
    (for [i (range n)]
      (= (get-in board [(+ (* dx i) x)
                        (+ (* dy i) y)])
         owner))))

(defn win? [owner board n]
  (some true?
    (for [i (range BOARD_SIZE)
          j (range BOARD_SIZE)
          dir [[1 0] [0 1] [1 1] [1 -1]]]
      (win-line? owner board [i j] dir n))))

(defn player-win? [board n]
  (win? :player board n))


(defn full? [board]
  (every? #{:ai :player} (apply concat board)))


(defn ai-win? [board n]
  (win? :ai board n))

(defn ai-move []
  (let [all-blanks (for [i (range BOARD_SIZE) j (range BOARD_SIZE)
                          :when (= :blank (get-in @app-state [:board i j]))] [i j])
        point (rand-nth all-blanks)]
    (swap! app-state assoc-in [:board (point 0) (point 1)] :ai)))

(defn header []
  [:div
    [:h1 (:text @app-state)]
    [:h2 (:game-status @app-state)]])

(defn drawrect [i j]
  [:rect {:width 0.9 :height 0.9
          :x (+ i 0.05)
          :y (+ j 0.05)
          :fill "gray"
          :on-click (fn my-click [event] ()
                      (when (and (not (full? (:board @app-state)))
                              (= :in-progress (:game-status @app-state)))
                        (swap! app-state assoc-in [:board i j] :player)
                        (ai-move))
                      (when (ai-win? (:board @app-state) WINNING_SIZE)
                        (swap! app-state assoc-in [:game-status] :ai-win))
                      (when (player-win? (:board @app-state) WINNING_SIZE)
                        (swap! app-state assoc-in [:game-status] :player-win)))}])

; (prn "did i win?"(win? :player (@app-state :board) 3))
; (prn "did i win?"(win? :player [[:player :ai :blank] [:ai :player :ai] [:blank :blank :player]] 3))
;
(defn drawcircle [i j]
  [:circle {:r 0.26
            :cx (+ i 0.5)
            :cy (+ j 0.5)
            :fill "none"
            :stroke "red"
            :stroke-width 0.08}])

(defn drawcross [i j]
  [:g {:stroke "green" :stroke-width 0.2
       :transform (str "translate(" (+ 0.5 i) "," (+ 0.5 j) ")"
                     "scale(0.2)")
       :stroke-linecap "round"}
   [:line {:x1 -1 :y1 -1 :x2 1 :y2 1}]
   [:line {:x1 -1 :y1  1 :x2  1 :y2 -1}]])


(defn body []
  [:div
    (into
     [:svg {:view-box [0 0 BOARD_SIZE BOARD_SIZE]
            :width 500
            :height 500}]
     (for [i (range BOARD_SIZE)
           j (range BOARD_SIZE)]
        (cond
          (= :blank (get-in @app-state [:board i j]))
          [drawrect i j]
          (= :ai (get-in @app-state [:board i j]))
          [drawcircle i j]
          (= :player (get-in @app-state [:board i j]))
          [drawcross i j])))])

(defn game []
  [:center
    [:div
      [header]
      [body]
      [:button {:on-click
                  (fn reset-btn [event]
                    (swap! app-state assoc-in [:board] (init-board BOARD_SIZE))
                    (swap! app-state assoc-in [:game-status] :in-progress))}
        "reset"]]])

(reagent/render-component [game]
                          (. js/document (getElementById "app")))
(defn on-js-reload []
  ;; optionally touch your app-state to force rerendering depending on
  ;; your application
  ;; (swap! app-state update-in [:__figwheel_counter] inc)
  (prn @app-state))
