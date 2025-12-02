package FlappyBird;

import java.awt.*;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
import java.io.File;
import java.io.IOException;

public class Tuberia {
    public int x, y, ancho, alto;
    public boolean contada;
    public int id;
    public boolean esSuperior;
    
    // Variables para las imágenes
    private static BufferedImage imagenStump;
    private static BufferedImage imagenStumpInvertido;
    private static boolean imagenesCargadas = false;
    
    // Mantenemos las dimensiones originales para la lógica del juego
    public static final int ANCHO_TUBERIA = 80;

    public Tuberia(int x, int y, int ancho, int alto, int id, boolean esSuperior) {
        this.x = x;
        this.y = y;
        this.ancho = ancho;
        this.alto = alto;
        this.contada = false;
        this.id = id;
        this.esSuperior = esSuperior;
        
        // Cargar las imágenes solo una vez
        if (!imagenesCargadas) {
            cargarImagenes();
        }
    }
    
    private void cargarImagenes() {
        try {
            // Cargar la imagen del tronco normal
            imagenStump = ImageIO.read(new File("src\\data\\stump.png"));
            
            // Cargar la imagen del tronco invertido
            imagenStumpInvertido = ImageIO.read(new File("src\\data\\stumpi.png"));
            
            imagenesCargadas = true;
        } catch (IOException e) {
            System.err.println("Error cargando imágenes de tuberías: " + e.getMessage());
            // Si hay error, usar colores por defecto
            e.printStackTrace();
        }
    }

    public void mover() {
        x -= 5;
    }

    public void dibujar(Graphics g) {
        if (imagenesCargadas) {
            // Usar las imágenes para dibujar
            BufferedImage imagen = esSuperior ? imagenStumpInvertido : imagenStump;
            
            // Escalar la imagen para que coincida con las dimensiones de la tubería
            // Mantenemos el ancho original (80) pero ajustamos la altura
            int alturaImagen = alto;
            
            // Para tuberías superiores, necesitamos dibujar desde la parte inferior hacia arriba
            if (esSuperior) {
                int yDibujo = y + alto - alturaImagen;
                g.drawImage(imagen, x, yDibujo, ancho, alturaImagen, null);
            } else {
                // Para tuberías inferiores, dibujar normalmente
                g.drawImage(imagen, x, y, ancho, alturaImagen, null);
            }
            
            // También dibujar un borde para mejor visibilidad (opcional)
            g.setColor(Color.DARK_GRAY);
            g.drawRect(x, y, ancho, alto);
        } else {
            // Fallback a los colores originales si las imágenes no se cargaron
            g.setColor(Color.GREEN);
            g.fillRect(x, y, ancho, alto);

            g.setColor(Color.DARK_GRAY);
            g.fillRect(x - 5, y, 5, alto);
            g.fillRect(x + ancho, y, 5, alto);
        }
    }

    public boolean colisionaCon(Pajaro pajaro) {
        // Mantener la misma lógica de colisión
        return x < pajaro.x + Pajaro.ANCHO &&
                x + ancho > pajaro.x &&
                y < pajaro.y + Pajaro.ALTO &&
                y + alto > pajaro.y;
    }
}