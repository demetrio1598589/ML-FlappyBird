package FlappyBird;

import java.awt.*;

public class Tuberia {
    public int x, y, ancho, alto;
    public boolean contada;
    public int id;
    public boolean esSuperior;

    public Tuberia(int x, int y, int ancho, int alto, int id, boolean esSuperior) {
        this.x = x;
        this.y = y;
        this.ancho = ancho;
        this.alto = alto;
        this.contada = false;
        this.id = id;
        this.esSuperior = esSuperior;
    }

    public void mover() {
        x -= 5;
    }

    public void dibujar(Graphics g) {
        g.setColor(Color.GREEN);
        g.fillRect(x, y, ancho, alto);

        g.setColor(Color.DARK_GRAY);
        g.fillRect(x - 5, y, 5, alto);
        g.fillRect(x + ancho, y, 5, alto);
    }

    public boolean colisionaCon(Pajaro pajaro) {
        return x < pajaro.x + Pajaro.ANCHO &&
                x + ancho > pajaro.x &&
                y < pajaro.y + Pajaro.ALTO &&
                y + alto > pajaro.y;
    }
}

