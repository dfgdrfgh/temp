// SPDX-License-Identifier: GPL-3.0-or-later
package moe.nea.firmament.prices;
import java.util.function.IntUnaryOperator;

public final class KeyInput {
    public static int keyForScancode(int scancode, IntUnaryOperator scanForKey) {
        for (int key = 32; key <= 348; key++) if (scanForKey.applyAsInt(key) == scancode) return key;
        return -1;
    }
    private KeyInput() {}
}
