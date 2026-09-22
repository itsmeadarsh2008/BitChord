import SwiftUI
import sharedKit

struct ContentView: View {
    var body: some View {
        VStack(spacing: 12) {
            Text("BitChord iOS shell")
                .font(.headline)
            // Proof the shared Kotlin framework links. Full player UI lands
            // in Phase 2 once PlayerController/StreamResolver get real
            // AVPlayer implementations.
            Text(ListenTogetherKt.greeting())
                .font(.subheadline)
                .multilineTextAlignment(.center)
                .padding()
        }
        .padding()
    }
}

#Preview {
    ContentView()
}
