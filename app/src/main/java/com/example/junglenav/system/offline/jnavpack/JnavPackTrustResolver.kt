package com.example.junglenav.system.offline.jnavpack

import com.example.junglenav.core.model.MapPackTrust

class JnavPackTrustResolver {
    fun resolve(manifest: JnavPackManifest): MapPackTrust {
        return if (!manifest.signature.isNullOrBlank()) {
            MapPackTrust.VERIFIED
        } else {
            MapPackTrust.UNVERIFIED
        }
    }
}
