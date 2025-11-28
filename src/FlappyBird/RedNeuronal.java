package FlappyBird;

import java.util.Random;

public class RedNeuronal {
    private double[][] pesosEntradaOculta;
    private double[][] pesosOcultaSalida;

    private static final int ENTRADAS = 2;
    private static final int OCULTAS = 5;
    private static final int SALIDAS = 1;

    public RedNeuronal() {
        pesosEntradaOculta = new double[ENTRADAS][OCULTAS];
        pesosOcultaSalida = new double[OCULTAS][SALIDAS];
        inicializarPesosAleatorios();
    }

    private void inicializarPesosAleatorios() {
        Random rand = new Random();
        for (int i = 0; i < ENTRADAS; i++) {
            for (int j = 0; j < OCULTAS; j++) {
                pesosEntradaOculta[i][j] = rand.nextDouble() * 2 - 1;
            }
        }
        for (int i = 0; i < OCULTAS; i++) {
            for (int j = 0; j < SALIDAS; j++) {
                pesosOcultaSalida[i][j] = rand.nextDouble() * 2 - 1;
            }
        }
    }

    public double predecir(double distanciaX, double distanciaYHueco) {
        double[] entradas = {distanciaX, distanciaYHueco};
        double[] oculta = new double[OCULTAS];

        // Capa oculta
        for (int j = 0; j < OCULTAS; j++) {
            oculta[j] = 0;
            for (int i = 0; i < ENTRADAS; i++) {
                oculta[j] += entradas[i] * pesosEntradaOculta[i][j];
            }
            oculta[j] = sigmoid(oculta[j]);
        }

        // Capa salida
        double salida = 0;
        for (int i = 0; i < OCULTAS; i++) {
            salida += oculta[i] * pesosOcultaSalida[i][0];
        }
        salida = sigmoid(salida);

        return salida;
    }

    private double sigmoid(double x) {
        return 1 / (1 + Math.exp(-x));
    }

    public RedNeuronal cruzar(RedNeuronal pareja) {
        RedNeuronal hijo = new RedNeuronal();
        Random rand = new Random();

        for (int i = 0; i < ENTRADAS; i++) {
            for (int j = 0; j < OCULTAS; j++) {
                if (rand.nextBoolean()) {
                    hijo.pesosEntradaOculta[i][j] = this.pesosEntradaOculta[i][j];
                } else {
                    hijo.pesosEntradaOculta[i][j] = pareja.pesosEntradaOculta[i][j];
                }
            }
        }

        for (int i = 0; i < OCULTAS; i++) {
            for (int j = 0; j < SALIDAS; j++) {
                if (rand.nextBoolean()) {
                    hijo.pesosOcultaSalida[i][j] = this.pesosOcultaSalida[i][j];
                } else {
                    hijo.pesosOcultaSalida[i][j] = pareja.pesosOcultaSalida[i][j];
                }
            }
        }

        return hijo;
    }

    public RedNeuronal clonar() {
        RedNeuronal clon = new RedNeuronal();
        for (int i = 0; i < ENTRADAS; i++) {
            System.arraycopy(pesosEntradaOculta[i], 0, clon.pesosEntradaOculta[i], 0, OCULTAS);
        }
        for (int i = 0; i < OCULTAS; i++) {
            System.arraycopy(pesosOcultaSalida[i], 0, clon.pesosOcultaSalida[i], 0, SALIDAS);
        }
        return clon;
    }

    public void mutar(double tasaMutacion) {
        Random rand = new Random();
        for (int i = 0; i < ENTRADAS; i++) {
            for (int j = 0; j < OCULTAS; j++) {
                if (rand.nextDouble() < tasaMutacion) {
                    pesosEntradaOculta[i][j] += rand.nextGaussian() * 0.1;
                }
            }
        }
        for (int i = 0; i < OCULTAS; i++) {
            for (int j = 0; j < SALIDAS; j++) {
                if (rand.nextDouble() < tasaMutacion) {
                    pesosOcultaSalida[i][j] += rand.nextGaussian() * 0.1;
                }
            }
        }
    }
}

