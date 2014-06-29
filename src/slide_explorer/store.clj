(ns slide-explorer.store
  (:require [slide-explorer.image :as image]
            [slide-explorer.tiles :as tiles]
            [slide-explorer.tile-cache :as tile-cache]))

(defn child-xy-index
  "Converts an x,y index to one in a child (1/2x zoom)."
  [n]
  (tiles/floor-int (/ n 2)))

(defn child-indices [tile-indices]
  "Compute the indices of a child of this tile (1/2x zoom)."
  (-> tile-indices
     (update-in [:nx] child-xy-index)
     (update-in [:ny] child-xy-index)
     (update-in [:zoom] / 2)))

(defn propagate-tile
  "Create a child tile from a parent tile, and store it
   in the memory-tile-atom."
  [memory-tiles-atom child-index parent-index]
  (let [child-tile (tile-cache/load-tile memory-tiles-atom child-index)
        parent-tile (tile-cache/load-tile memory-tiles-atom parent-index)
        new-child-tile (image/insert-quadrant
                         parent-tile
                         [(even? (:nx parent-index))
                          (even? (:ny parent-index))]
                          child-tile)]
    (tile-cache/add-tile memory-tiles-atom child-index new-child-tile)))

(defn add-to-memory-tiles
  "Adds a tile to the memory-tiles-atom, including various zoom levels."
  [memory-tiles-atom indices tile min-zoom]
  (let [full-indices (assoc indices :zoom 1)]
    (tile-cache/add-tile memory-tiles-atom full-indices tile)
    (loop [child-index (child-indices full-indices)
           parent-index full-indices]
      (when (<= min-zoom (:zoom child-index))
        (propagate-tile memory-tiles-atom child-index parent-index)
        (recur (child-indices child-index) child-index)))))