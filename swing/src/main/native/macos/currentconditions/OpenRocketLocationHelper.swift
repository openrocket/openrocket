import AppKit
import CoreLocation
import Darwin

final class LocationDelegate: NSObject, CLLocationManagerDelegate {
    private let manager = CLLocationManager()
    private var started = false
    private var timeout: Timer?

    func start() {
        timeout = Timer.scheduledTimer(withTimeInterval: 15, repeats: false) { _ in
            self.fail("Location Services timed out.")
        }
        manager.delegate = self
        manager.desiredAccuracy = kCLLocationAccuracyBest

        switch manager.authorizationStatus {
        case .notDetermined:
            manager.requestWhenInUseAuthorization()
        case .authorized, .authorizedAlways:
            startLocationUpdates()
        default:
            fail("Location access is disabled or denied. Enable it in Privacy & Security > Location Services.")
        }
    }

    func locationManagerDidChangeAuthorization(_ manager: CLLocationManager) {
        switch manager.authorizationStatus {
        case .authorized, .authorizedAlways:
            startLocationUpdates()
        case .denied, .restricted:
            fail("Location access was denied. Enable it in Privacy & Security > Location Services.")
        default:
            break
        }
    }

    private func startLocationUpdates() {
        guard !started else { return }
        started = true
        manager.startUpdatingLocation()
    }

    func locationManager(_ manager: CLLocationManager, didUpdateLocations locations: [CLLocation]) {
        guard let location = locations.last,
              location.horizontalAccuracy >= 0,
              abs(location.timestamp.timeIntervalSinceNow) <= 30 else { return }
        timeout?.invalidate()
        manager.stopUpdatingLocation()
        let output = String(
            format: "%.8f\t%.8f\t%.2f\t%.2f",
            locale: Locale(identifier: "en_US_POSIX"),
            location.coordinate.latitude,
            location.coordinate.longitude,
            location.altitude,
            location.horizontalAccuracy
        )
        print(output)
        fflush(stdout)
        NSApplication.shared.terminate(nil)
    }

    func locationManager(_ manager: CLLocationManager, didFailWithError error: Error) {
        if let locationError = error as? CLError, locationError.code == .locationUnknown {
            return
        }
        fail(error.localizedDescription)
    }

    private func fail(_ message: String) -> Never {
        fputs(message + "\n", stderr)
        fflush(stderr)
        exit(1)
    }
}

let application = NSApplication.shared
application.setActivationPolicy(.accessory)
application.activate(ignoringOtherApps: true)
let locationDelegate = LocationDelegate()
locationDelegate.start()
application.run()
