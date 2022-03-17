(ns com.github.lukebemish.pitchforks.client.shared
  (:require [com.github.lukebemish.clojurewrapper.api.util :as util]
            [com.github.lukebemish.pitchforks.shared :as shared])
  (:import (net.minecraft.client.model.geom ModelLayerLocation)))

(def pitchfork-layer (ModelLayerLocation. (util/resource-location shared/modid "pitchfork") "main"))