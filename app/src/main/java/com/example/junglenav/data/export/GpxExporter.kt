package com.example.junglenav.data.export

import com.example.junglenav.core.model.TrackPoint

class GpxExporter {
    fun export(name: String, points: List<TrackPoint>): String {
        val body = points.joinToString(separator = "") { point ->
            """
            <trkpt lat="${point.latitude}" lon="${point.longitude}">
              <ele>${point.altitudeMeters ?: 0.0}</ele>
              <extensions>
                <junglenav:position_mode>${point.positionMode.name}</junglenav:position_mode>
                <junglenav:confidence>${point.confidence}</junglenav:confidence>
              </extensions>
            </trkpt>
            """.trimIndent()
        }

        return "<gpx><trk><name>$name</name><trkseg>$body</trkseg></trk></gpx>"
    }
}
