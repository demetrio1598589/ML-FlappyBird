package FlappyBird;

import java.awt.*;
import java.util.Random;

public class Pajaro {
    public int x, y;
    public int velocidadY;
    public static final int ANCHO = 30;
    public static final int ALTO = 30;
    private static final int GRAVEDAD = 1;
    private static final int FUERZA_SALTO = -15;

    public RedNeuronal cerebro;
    public boolean vivo;
    public double aptitud;
    public int puntuacion;
    public double distanciaRecorrida;

    public Pajaro(int x, int y) {
        this.x = x;
        this.y = y;
        this.velocidadY = 0;
        this.vivo = true;
        this.aptitud = 0;
        this.puntuacion = 0;
        this.distanciaRecorrida = 0;
        this.cerebro = new RedNeuronal();
    }

    public void actualizar() {
        if (!vivo) return;

        velocidadY += GRAVEDAD;
        y += velocidadY;
        distanciaRecorrida += 1;
    }

    public void saltar() {
        if (!vivo) return;
        velocidadY = FUERZA_SALTO;
    }

    public void dibujar(Graphics g) {
        if (!vivo) return;

        g.setColor(Color.YELLOW);
        g.fillRect(x, y, ANCHO, ALTO);

        g.setColor(Color.BLACK);
        g.fillRect(x + 20, y + 10, 5, 5);

        g.setColor(Color.ORANGE);
        g.fillRect(x + 5, y + 15, 15, 8);
    }

    public void pensar(Tuberia tuberiaCercana) {
        if (!vivo || cerebro == null || tuberiaCercana == null) return;

        double distanciaX = (tuberiaCercana.x - x) / 100.0;
        double centroHuecoY = tuberiaCercana.y + tuberiaCercana.alto + (JuegoPrincipal.ESPACIO_TUBERIAS / 2.0);
        double distanciaYHueco = (centroHuecoY - y) / 100.0;

        double decision = cerebro.predecir(distanciaX, distanciaYHueco);

        if (decision > 0.7) {
            saltar();
        }
    }

    public void calcularAptitud() {
        // FITNESS BASADO EN EL EJEMPLO FUNCIONAL
        double fitnessBase = 1.0;

        // Recompensa principal por puntuación
        double fitnessPuntuacion = Math.pow(puntuacion + 1, 2) * 10;

        // Recompensa por distancia recorrida
        double fitnessDistancia = distanciaRecorrida * 0.5;

        this.aptitud = fitnessBase + fitnessPuntuacion + fitnessDistancia;

        // Asegurar fitness mínimo
        this.aptitud = Math.max(1.0, this.aptitud);
    }

    public Pajaro cruzar(Pajaro pareja) {
        Pajaro hijo = new Pajaro(this.x, this.y);
        hijo.cerebro = this.cerebro.cruzar(pareja.cerebro);
        return hijo;
    }

    public void mutar(double tasaMutacion) {
        cerebro.mutar(tasaMutacion);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Pajaro other = (Pajaro) obj;
        return this.x == other.x &&
                this.y == other.y &&
                this.aptitud == other.aptitud;
    }
}

