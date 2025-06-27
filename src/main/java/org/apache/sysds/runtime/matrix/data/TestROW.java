package org.apache.sysds.runtime.matrix.data;
import org.apache.sysds.runtime.data.DenseBlock;
import org.apache.sysds.runtime.data.DenseBlockFP64;
import org.apache.sysds.runtime.instructions.cp.KahanObject;
import org.apache.sysds.runtime.functionobjects.KahanPlus;

public class TestROW {

    public static void main(String[] args) {
        int rows = 2;
        int cols = 3;

        // Beispielmatrix (2x3)
        double[][] inputData = new double[][] {
                {1.0, 2.0, 3.0},
                {4.0, 5.0, 6.0}
        };

        // DenseBlock für Input
        DenseBlock a = new DenseBlockFP64(new int[]{rows, cols});
        for (int i = 0; i < rows; i++)
            for (int j = 0; j < cols; j++)
                a.set(i, j, inputData[i][j]);

        // DenseBlock für Output
        DenseBlock c = new DenseBlockFP64(new int[]{rows, cols});

        // Beispiel-agg (Startwert pro Zeile)
        double[] agg = new double[] {0, 0};

        // Sonstige Parameter
        int n = cols;       // Spaltenanzahl
        int rl = 0;         // Startreihe
        int ru = rows;      // Endreihe
        KahanObject kbuff = new KahanObject(0, 0);
        KahanPlus kplus = KahanPlus.getKahanPlusFnObject();

        // Aufruf der Methode
        d_urowcumkp(a, agg, c, n, kbuff, kplus, rl, ru);

        // Ausgabe Ergebnis
        System.out.println("\n=== Ergebnis-Matrix ===");
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                System.out.print(c.get(i, j) + "\t");
            }
            System.out.println();
        }
    }

    private static void d_urowcumkp(DenseBlock a, double[] agg, DenseBlock c, int n,
                                    KahanObject kbuff, KahanPlus kplus, int rl, int ru) {
        System.out.println("=== d_urowcumkp Debug Start ===");

        if (agg != null && agg.length != (ru - rl)) {
            System.err.println("⚠️ WARNUNG: agg.length != ru - rl → agg.length = "
                    + agg.length + ", ru - rl = " + (ru - rl));
        }

        for (int i = rl; i < ru; i++) {
            System.out.println("\n--- Row i=" + i + " ---");

            double start = 0.0;
            int localRow = i - rl;
            if (agg != null && localRow >= 0 && localRow < agg.length) {
                start = agg[localRow];
                System.out.printf("Using agg[%d] = %.12f\n", localRow, start);
            }

            kbuff.set(start, 0);
            for (int j = 0; j < n; j++) {
                double val = a.get(i, j);
                kplus.execute2(kbuff, val);
                c.set(i, j, kbuff._sum);
            }
        }

        System.out.println("=== d_urowcumkp Debug End ===\n");
    }
}
