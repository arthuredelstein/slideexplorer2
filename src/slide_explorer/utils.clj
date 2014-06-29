(ns slide-explorer.utils
  (:import (javax.swing JFrame JTextArea)))

(def testing
  "An atom that indicates if we are currently
   in testing mode."
  (atom false))

;; identify OS

(defn get-os
  "Returns the operating system name as provided
   by the JVM."
  []
  (.. System (getProperty "os.name") toLowerCase))

(def is-win
  "Returns true if we are running on Windows."
  (memoize #(not (neg? (.indexOf (get-os) "win")))))

(def is-mac
  "Returns true if we are running on Mac"
  (memoize #(not (neg? (.indexOf (get-os) "mac")))))

(def is-unix
  "Returns trun if we are running on Linux or Unix."
  (memoize #(not (and (neg? (.indexOf (get-os) "nix"))
                      (neg? (.indexOf (get-os) "nux"))))))

(defn show
  "Log a value and return that value."
  [x]
  (do (pr x)
      x))

(defn- display-data-frame
  "Generates the frame that is used by display-data."
  [ref]
  (let [f (JFrame. (.toString ref))
        t (JTextArea.)
        show-val #(.setText t
                           (with-out-str
                             (clojure.pprint/pprint %)))]
    (.add (.getContentPane f) t)
    (add-watch ref ::display
               (fn [_ _ old-val new-val]
                 (when-not (= old-val new-val) (show-val new-val))))
    (show-val @ref)
    (doto f (.setBounds 20 50 400 400))))

(defn display-data
  "Show a frame that displays the data in real time."
  [ref]
  (-> (alter-meta! ref update-in [::display] #(or % (display-data-frame ref)))
      ::display .show))