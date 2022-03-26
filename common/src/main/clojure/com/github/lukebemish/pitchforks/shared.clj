(ns com.github.lukebemish.pitchforks.shared
  (:import (org.apache.logging.log4j LogManager Logger)))


(def ^String modid "pitchforks")
(def ^Logger logger (LogManager/getLogger modid))
