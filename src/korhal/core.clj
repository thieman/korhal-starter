(ns korhal.core
  (:require [korhal.interop.interop :refer :all]
            [korhal.tools.util :refer [swap-key swap-keys profile]]
            [korhal.tools.repl :refer :all]
            [korhal.tools.queue :refer :all])
  (:import (clojure.lang.IDeref)
           (jnibwapi.JNIBWAPI)
           (jnibwapi.BWAPIEventListener)))

(gen-class
 :name "korhal.core"
 :implements [jnibwapi.BWAPIEventListener clojure.lang.IDeref]
 :state state
 :init init
 :main true
 :constructors {[] []}
 :prefix "korhal-")

(defn korhal-deref [this] @(.state this))

(defn korhal-main [& args]
  (let [ai (korhal.core.)
        api (jnibwapi.JNIBWAPI. ai)]
    (swap! (.state ai) swap-key :api api)
    (bind-api! api)
    (start)))

(defn korhal-init []
  [[] (atom {})])

(defn korhal-connected [this]
  (load-type-data))

(defn korhal-gameStarted [this]
  (println "Game Started")
  (enable-user-input)
  (set-game-speed 10)
  (load-map-data true))

(defn can-afford? [unit-kw]
  (let [unit-type (get-unit-type (unit-type-kws unit-kw))]
    (and (>= (my-minerals) (mineral-price unit-type))
         (>= (my-gas) (gas-price unit-type))
         (>= (- (my-supply-total) (my-supply-used))
             (supply-required unit-type)))))

(defn korhal-gameUpdate [this]

  ;; start mining
  (doseq [drone (filter idle? (my-drones))]
    (let [dist-to-drone (fn [mineral] (dist drone mineral))
          closest-mineral (apply min-key dist-to-drone (minerals))]
      (right-click drone closest-mineral)))

  ;; build up to 6 drones
  (when (and (can-afford? :drone)
             (< (my-supply-used) 6))
    (let [larva (first (my-larvae))]
      (morph larva :drone)))

  ;; build spawning pool with one of those drones
  (when (and (can-afford? :spawning-pool)
             (zero? (count (my-spawning-pools))))
    (let [drone (first (filter completed? (my-drones)))
          hatchery (first (my-hatcheries))
          tx (if (< (tile-x hatchery) 40) (+ 5 (tile-x hatchery)) (- (tile-x hatchery) 5))
          ty (tile-y hatchery)]
      (build drone tx ty :spawning-pool)))

  ;; build zerglings
  (when (and (can-afford? :zergling)
             (not (empty? (filter completed? (my-spawning-pools)))))
    (let [larva (first (my-larvae))]
      (morph larva :zergling)))

  ;; rush the shit out of them
  (let [enemy-base (first (enemy-start-locations))]
    (doseq [zergling (filter idle? (my-zerglings))]
      (attack zergling (pixel-x enemy-base) (pixel-y enemy-base))))

  )

(defn korhal-gameEnded [this])

(defn korhal-keyPressed [this keycode])

(defn korhal-matchEnded [this winner])
(defn korhal-sendText [this text])

(defn korhal-receiveText [this text])

(defn korhal-nukeDetect [this x y])

(defn korhal-playerLeft [this player-id])

(defn korhal-unitCreate [this unit-id])

(defn korhal-unitDestroy [this unit-id])

(defn korhal-unitDiscover [this unit-id])

(defn korhal-unitEvade [this unit-id])

(defn korhal-unitHide [this unit-id])

(defn korhal-unitMorph [this unit-id])

(defn korhal-unitShow [this unit-id])

(defn korhal-unitRenegade [this unit-id])

(defn korhal-saveGame [this game-name])

(defn korhal-unitComplete [this unit-id])

(defn korhal-playerDropped [this player-id])
