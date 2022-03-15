(ns com.github.lukebemish.pitchforks.entity.shared
  (:import (net.minecraft.network.syncher SynchedEntityData EntityDataSerializers)))

(def id-loyalty (memoize (fn [] (SynchedEntityData/defineId (import com.github.lukebemish.pitchforks.entity.thrownpitchfork) EntityDataSerializers/BYTE))))
(def id-foil (memoize (fn [] (SynchedEntityData/defineId (import com.github.lukebemish.pitchforks.entity.thrownpitchfork) EntityDataSerializers/BOOLEAN))))