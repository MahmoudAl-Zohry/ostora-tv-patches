/*
 * Ostora TV 7.0 VPN transport-check patch for Morphe.
 * Targets: com.t4w.ostora516, versionName 7.0.
 *
 * Static-analysis basis:
 * A unique boolean method obtains the "connectivity" service, checks
 * NetworkCapabilities.hasTransport(4) on the active network, then repeats
 * the check across getAllNetworks(). Transport type 4 is TRANSPORT_VPN.
 */
package app.morphe.patches.ostora.vpn

import app.morphe.patcher.Fingerprint
import app.morphe.patcher.literal
import app.morphe.patcher.methodCall
import app.morphe.patcher.string
import app.morphe.patcher.patch.BytecodePatch
import app.morphe.patcher.patch.booleanOption
import app.morphe.patcher.patch.bytecodePatch
import app.morphe.patcher.util.matchSingle
import app.morphe.patcher.util.returnEarly

private object OstoraVpnTransportFingerprint : Fingerprint(
    returnType = "Z",
    parameters = emptyList(),
    filters = listOf(
        string("connectivity"),
        methodCall(
            definingClass = "Landroid/content/Context;",
            name = "getSystemService",
        ),
        literal(4),
        methodCall(
            definingClass = "Landroid/net/ConnectivityManager;",
            name = "getNetworkCapabilities",
        ),
        methodCall(
            definingClass = "Landroid/net/NetworkCapabilities;",
            name = "hasTransport",
        ),
        methodCall(
            definingClass = "Landroid/net/ConnectivityManager;",
            name = "getAllNetworks",
        ),
        methodCall(
            definingClass = "Landroid/net/ConnectivityManager;",
            name = "getNetworkCapabilities",
        ),
        methodCall(
            definingClass = "Landroid/net/NetworkCapabilities;",
            name = "hasTransport",
        ),
    ),
)

@Suppress("unused")
val ostoraVpnTransportPatch: BytecodePatch = bytecodePatch(
    name = "Ostora TV — Bypass VPN transport check",
    description = "Skips the local Android VPN transport detector in Ostora TV 7.0.",
    default = true,
) {
    compatibleWith("com.t4w.ostora516"("7.0"))

    val bypassVpnTransport by booleanOption(
        key = "bypassVpnTransport",
        default = true,
        title = "Bypass VPN transport check",
        description = "Makes the app's local VPN transport-detection method return false.",
    )

    execute {
        if (bypassVpnTransport) {
            OstoraVpnTransportFingerprint.matchSingle().method.returnEarly(false)
        }
    }
}
