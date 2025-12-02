package FlappyBird;

import javax.sound.sampled.*;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.Random;
import java.util.HashSet;
import java.util.Set;
import java.util.Collections;
import java.awt.image.BufferedImage;
import java.io.File;

public class JuegoPrincipal extends JPanel implements ActionListener, KeyListener {
    private Timer timer;
    private Pajaro pajaro;
    private ArrayList<Pajaro> poblacion;
    private ArrayList<Pajaro> poblacionCompletadores;
    private ArrayList<Tuberia> tuberias;
    private int puntuacion;
    private int mejorPuntuacion;
    private boolean juegoActivo;
    private boolean modoEntrenamiento;
    private boolean entrenamientoPausado;
    private Generacion generacion;
    private int vidas;
    private int siguienteIdTuberia = 0;

    // Sistema de niveles
    private int nivel;
    private int tuberiasPasadasNivel;
    private static final int TUBERIAS_POR_NIVEL = 10;
    private static final int TOTAL_NIVELES = 10;
    private boolean cambiandoNivel;
    private int contadorCambioNivel;
    private boolean entrenamientoCompletado;

    private Image imgFondoDia;
    private Image imgFondoNoche;
    private Image imgBase;
    // Variables para la animación de la base
    private int baseScrollX1 = 0;
    private int baseScrollX2;
    private static final int VELOCIDAD_BASE = 2;
    
    // Variables para sonidos
    private Clip sonidoSalto;
    private Clip sonidoColision;
    private Clip sonidoMuerte;
    private boolean sonidosCargados = false;

    // Constantes del juego
    private static final int ANCHO = 1000;
    private static final int ALTO = 700;
    private static final int GRAVEDAD = 1;
    private static final int FUERZA_SALTO = -15;
    public static int ESPACIO_TUBERIAS = 250;
    private static final int ANCHO_TUBERIA = 80;
    private static int DISTANCIA_TUBERIAS = 350;

    // Panel de control
    private JPanel panelControl;
    private JLabel lblGeneracion;
    private JLabel lblMejorFitness;
    private JLabel lblMejorRecord;
    private JButton btnIniciarML;
    private JButton btnPausarML;
    private JButton btnReiniciarML;
    private JButton btnContinuarEntrenamiento;
    private JLabel lblPuntuacion;
    private JLabel lblVidas;
    private JLabel lblMejorPuntuacion;
    private JLabel lblNivel;
    private JLabel lblTop5;

    // Paneles modales
    private JPanel panelModoLibre;
    private JPanel panelMachineLearning;

    // Variables para mantener el progreso entre continuaciones
    private int generacionAnterior;
    private int mejorRecordAnterior;
    private double mejorFitnessAnterior;

    // Modificar el constructor para cargar las imágenes y sonidos
    public JuegoPrincipal() {
        setPreferredSize(new Dimension(ANCHO, ALTO));
        setLayout(new BorderLayout());

        timer = new Timer(14, this);
        puntuacion = 0;
        mejorPuntuacion = 0;
        juegoActivo = false;
        modoEntrenamiento = false;
        entrenamientoPausado = false;
        entrenamientoCompletado = false;
        vidas = 3;
        nivel = 1;
        tuberiasPasadasNivel = 0;
        cambiandoNivel = false;
        poblacionCompletadores = new ArrayList<>();

        generacionAnterior = 0;
        mejorRecordAnterior = 0;
        mejorFitnessAnterior = 0;

        // Cargar las imágenes y sonidos
        cargarImagenes();
        cargarSonidos();
        
        crearPanelControl();
        addKeyListener(this);
        setFocusable(true);
       
        try {
            Pajaro.sprites[0] = Toolkit.getDefaultToolkit().getImage("src\\data\\bird1.png");
            Pajaro.sprites[1] = Toolkit.getDefaultToolkit().getImage("src\\data\\bird2.png");
            Pajaro.sprites[2] = Toolkit.getDefaultToolkit().getImage("src\\data\\bird3.png");
            Pajaro.setJuegoRef(this);Pajaro.setJuegoRef(this);
        } catch (Exception e) {
            e.printStackTrace();
        }

        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                requestFocusInWindow();
            }
        });
    }
    
    private void cargarSonidos() {
        try {
            // Cargar sonido de salto (wing.mp3)
            File archivoSalto = new File("src\\data\\wing.wav");
            if (archivoSalto.exists()) {
                AudioInputStream audioInputStream = AudioSystem.getAudioInputStream(archivoSalto);
                sonidoSalto = AudioSystem.getClip();
                sonidoSalto.open(audioInputStream);
            } else {
                System.err.println("No se encontró wing.mp3 en src\\data\\");
            }
            
            // Cargar sonido de colisión (hit.mp3)
            File archivoColision = new File("src\\data\\hit.wav");
            if (archivoColision.exists()) {
                AudioInputStream audioInputStream = AudioSystem.getAudioInputStream(archivoColision);
                sonidoColision = AudioSystem.getClip();
                sonidoColision.open(audioInputStream);
            } else {
                System.err.println("No se encontró hit.mp3 en src\\data\\");
            }
            
            // Cargar sonido de muerte (die.mp3)
            File archivoMuerte = new File("src\\data\\die.wav");
            if (archivoMuerte.exists()) {
                AudioInputStream audioInputStream = AudioSystem.getAudioInputStream(archivoMuerte);
                sonidoMuerte = AudioSystem.getClip();
                sonidoMuerte.open(audioInputStream);
            } else {
                System.err.println("No se encontró die.mp3 en src\\data\\");
            }
            
            sonidosCargados = true;
            
        } catch (Exception e) {
            System.err.println("Error cargando sonidos: " + e.getMessage());
            sonidosCargados = false;
        }
    }
    
    public void reproducirSonidoSalto() {
        if (sonidosCargados && sonidoSalto != null) {
            // Reiniciar el sonido si ya está reproduciéndose
            if (sonidoSalto.isRunning()) {
                sonidoSalto.stop();
            }
            sonidoSalto.setFramePosition(0);
            sonidoSalto.start();
        }
    }
    
    public void reproducirSonidoColision() {
        if (sonidosCargados && sonidoColision != null) {
            // No reiniciamos si ya está sonando, para que se complete
            if (!sonidoColision.isRunning()) {
                sonidoColision.setFramePosition(0);
                sonidoColision.start();
            }
        }
    }
    
    public void reproducirSonidoMuerte() {
        if (sonidosCargados && sonidoMuerte != null) {
            // Esperar un momento para que el sonido de colisión termine
            new Thread(() -> {
                try {
                    Thread.sleep(100); // Pequeña pausa
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                if (!sonidoMuerte.isRunning()) {
                    sonidoMuerte.setFramePosition(0);
                    sonidoMuerte.start();
                }
            }).start();
        }
    }
    
    private void cerrarSonidos() {
        if (sonidoSalto != null) {
            sonidoSalto.close();
        }
        if (sonidoColision != null) {
            sonidoColision.close();
        }
        if (sonidoMuerte != null) {
            sonidoMuerte.close();
        }
    }

    private void crearPanelControl() {
        panelControl = new JPanel();
        panelControl.setPreferredSize(new Dimension(250, ALTO));
        panelControl.setBackground(Color.LIGHT_GRAY);
        panelControl.setLayout(new BoxLayout(panelControl, BoxLayout.Y_AXIS));

        // Título
        JLabel titulo = new JLabel("FLAPPY BIRD");
        titulo.setFont(new Font("Arial", Font.BOLD, 20));
        titulo.setAlignmentX(Component.CENTER_ALIGNMENT);
        panelControl.add(titulo);
        panelControl.add(Box.createRigidArea(new Dimension(0, 20)));

        // Botones de modo
        JButton btnJugarLibre = new JButton("JUGAR LIBRE");
        JButton btnMachineLearning = new JButton("MACHINE LEARNING");

        btnJugarLibre.setAlignmentX(Component.CENTER_ALIGNMENT);
        btnMachineLearning.setAlignmentX(Component.CENTER_ALIGNMENT);

        btnJugarLibre.addActionListener(e -> iniciarModoLibre());
        btnMachineLearning.addActionListener(e -> iniciarModoML());

        panelControl.add(btnJugarLibre);
        panelControl.add(Box.createRigidArea(new Dimension(0, 10)));
        panelControl.add(btnMachineLearning);
        panelControl.add(Box.createRigidArea(new Dimension(0, 30)));

        // Panel Jugar Libre (inicialmente oculto)
        panelModoLibre = new JPanel();
        panelModoLibre.setLayout(new BoxLayout(panelModoLibre, BoxLayout.Y_AXIS));
        panelModoLibre.setBorder(BorderFactory.createTitledBorder("Jugar Libre"));
        panelModoLibre.setVisible(false);

        lblPuntuacion = new JLabel("Puntuación: 0");
        lblMejorPuntuacion = new JLabel("Mejor: 0");
        lblVidas = new JLabel("Vidas: 3");
        lblNivel = new JLabel("Nivel: 1");

        panelModoLibre.add(lblPuntuacion);
        panelModoLibre.add(lblMejorPuntuacion);
        panelModoLibre.add(lblVidas);
        panelModoLibre.add(lblNivel);

        // Panel Machine Learning (inicialmente oculto)
        panelMachineLearning = new JPanel();
        panelMachineLearning.setLayout(new BoxLayout(panelMachineLearning, BoxLayout.Y_AXIS));
        panelMachineLearning.setBorder(BorderFactory.createTitledBorder("Machine Learning"));
        panelMachineLearning.setVisible(false);

        btnIniciarML = new JButton("Iniciar");
        btnPausarML = new JButton("Pausar");
        btnReiniciarML = new JButton("Reiniciar");
        btnContinuarEntrenamiento = new JButton("Continuar Entrenando");

        btnIniciarML.setEnabled(true);
        btnPausarML.setEnabled(false);
        btnReiniciarML.setEnabled(true);
        btnContinuarEntrenamiento.setEnabled(false);
        btnContinuarEntrenamiento.setVisible(false);

        btnIniciarML.addActionListener(e -> iniciarEntrenamiento());
        btnPausarML.addActionListener(e -> pausarEntrenamiento());
        btnReiniciarML.addActionListener(e -> reiniciarEntrenamiento());
        btnContinuarEntrenamiento.addActionListener(e -> continuarEntrenamiento());

        panelMachineLearning.add(btnIniciarML);
        panelMachineLearning.add(Box.createRigidArea(new Dimension(0, 5)));
        panelMachineLearning.add(btnPausarML);
        panelMachineLearning.add(Box.createRigidArea(new Dimension(0, 5)));
        panelMachineLearning.add(btnReiniciarML);
        panelMachineLearning.add(Box.createRigidArea(new Dimension(0, 10)));
        panelMachineLearning.add(btnContinuarEntrenamiento);

        // Labels de estadísticas ML
        lblGeneracion = new JLabel("Generación: 0");
        lblMejorFitness = new JLabel("Mejor Fitness: 0");
        lblMejorRecord = new JLabel("Mejor Record: 0");
        lblTop5 = new JLabel("<html>Top 5:<br>0<br>0<br>0<br>0<br>0</html>");

        panelMachineLearning.add(Box.createRigidArea(new Dimension(0, 10)));
        panelMachineLearning.add(lblGeneracion);
        panelMachineLearning.add(lblMejorFitness);
        panelMachineLearning.add(lblMejorRecord);
        panelMachineLearning.add(Box.createRigidArea(new Dimension(0, 10)));
        panelMachineLearning.add(lblTop5);

        panelControl.add(panelModoLibre);
        panelControl.add(panelMachineLearning);

        add(panelControl, BorderLayout.EAST);
    }

    public void iniciarModoLibre() {
        modoEntrenamiento = false;
        entrenamientoPausado = false;
        entrenamientoCompletado = false;
        vidas = 3;
        nivel = 1;
        tuberiasPasadasNivel = 0;
        actualizarDificultad();

        panelModoLibre.setVisible(true);
        panelMachineLearning.setVisible(false);

        btnContinuarEntrenamiento.setVisible(false);
        actualizarUI();
        iniciarJuego();
        requestFocusInWindow();
    }
    private void cargarImagenes() {
        try {
            // Cargar imágenes de fondo
            imgFondoDia = Toolkit.getDefaultToolkit().getImage("src\\data\\dayCity.png");
            imgFondoNoche = Toolkit.getDefaultToolkit().getImage("src\\data\\nightCity.png");
            imgBase = Toolkit.getDefaultToolkit().getImage("src\\data\\base.png");
            
            // Esperar a que las imágenes se carguen
            MediaTracker tracker = new MediaTracker(this);
            tracker.addImage(imgFondoDia, 0);
            tracker.addImage(imgFondoNoche, 1);
            tracker.addImage(imgBase, 2);
            
            try {
                tracker.waitForAll();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            
            // Calcular posición inicial de la segunda base para scroll continuo
            baseScrollX2 = imgBase.getWidth(this);
            
        } catch (Exception e) {
            System.err.println("Error al cargar imágenes: " + e.getMessage());
            // Crear imágenes de respaldo en caso de error
            imgFondoDia = crearImagenRespaldo(new Color(135, 206, 235)); // Azul cielo
            imgFondoNoche = crearImagenRespaldo(new Color(25, 25, 112)); // Azul noche
            imgBase = crearImagenRespaldo(new Color(222, 184, 135)); // Color base
        }
    }
    // Método para crear imágenes de respaldo si las originales no se cargan
    private Image crearImagenRespaldo(Color color) {
        BufferedImage img = new BufferedImage(ANCHO, ALTO, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = img.createGraphics();
        g2d.setColor(color);
        g2d.fillRect(0, 0, ANCHO, ALTO);
        g2d.dispose();
        return img;
    }

    public void iniciarModoML() {
        modoEntrenamiento = true;
        entrenamientoPausado = false;
        entrenamientoCompletado = false;
        nivel = 1;
        tuberiasPasadasNivel = 0;
        actualizarDificultad();

        panelModoLibre.setVisible(false);
        panelMachineLearning.setVisible(true);

        btnIniciarML.setEnabled(true);
        btnPausarML.setEnabled(false);
        btnReiniciarML.setEnabled(true);
        btnContinuarEntrenamiento.setVisible(false);
        requestFocusInWindow();
    }

    public void iniciarJuego() {
        if (modoEntrenamiento) {
            if (generacion == null) {
                generacion = new Generacion(50);
            }
            poblacion = generacion.poblacion;
        } else {
            pajaro = new Pajaro(ANCHO / 4, ALTO / 2);
        }

        tuberias = new ArrayList<>();
        siguienteIdTuberia = 0;
        
        // Resetear posición de la base
        baseScrollX1 = 0;
        baseScrollX2 = imgBase != null ? imgBase.getWidth(this) : ANCHO;
        
        agregarTuberia();
        puntuacion = 0;
        tuberiasPasadasNivel = 0;
        juegoActivo = true;
        timer.start();
        actualizarUI();

        requestFocusInWindow();
    }

    public void iniciarEntrenamiento() {
        entrenamientoPausado = false;
        btnIniciarML.setEnabled(false);
        btnPausarML.setEnabled(true);
        btnContinuarEntrenamiento.setVisible(false);
        iniciarJuego();
    }

    public void continuarEntrenamiento() {
        if (poblacionCompletadores.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "No hay pájaros que hayan completado el entrenamiento.\n" +
                            "Usa 'Reiniciar' para empezar de nuevo.",
                    "Sin Completadores", JOptionPane.WARNING_MESSAGE);
            return;
        }

        entrenamientoCompletado = false;
        nivel = 1; // Reiniciar niveles pero mantener generaciones
        tuberiasPasadasNivel = 0;
        actualizarDificultad();

        // Crear nueva generación basada en los completadores, manteniendo el contador
        generacion = new Generacion(50, poblacionCompletadores, generacionAnterior, mejorRecordAnterior, mejorFitnessAnterior);

        btnContinuarEntrenamiento.setVisible(false);
        btnIniciarML.setEnabled(false);
        btnPausarML.setEnabled(true);
        iniciarJuego();

        System.out.println("Continuando entrenamiento - Generación: " + generacion.generacion +
                ", Mejor Record: " + generacion.mejorRecord);
    }

    public void pausarEntrenamiento() {
        entrenamientoPausado = !entrenamientoPausado;
        btnIniciarML.setEnabled(entrenamientoPausado);
        btnPausarML.setText(entrenamientoPausado ? "Reanudar" : "Pausar");
    }

    public void reiniciarEntrenamiento() {
        // Reiniciar completamente, incluyendo contadores
        generacion = new Generacion(50);
        nivel = 1;
        tuberiasPasadasNivel = 0;
        actualizarDificultad();
        actualizarUI();

        // Reiniciar también los contadores de continuación
        generacionAnterior = 0;
        mejorRecordAnterior = 0;
        mejorFitnessAnterior = 0;
        poblacionCompletadores.clear();

        if (juegoActivo) {
            timer.stop();
        }
        btnContinuarEntrenamiento.setVisible(false);
        iniciarEntrenamiento();

        System.out.println("Entrenamiento reiniciado completamente desde generación 1");
    }

    private void actualizarDificultad() {
        // NIVELES MÁS FÁCILES: niveles 1-4 muy fáciles, progresión suave
        switch (nivel) {
            case 1:
                ESPACIO_TUBERIAS = 320; // Muy amplio
                DISTANCIA_TUBERIAS = 370; // Muy separado
                break;
            case 2:
                ESPACIO_TUBERIAS = 300;
                DISTANCIA_TUBERIAS = 360;
                break;
            case 3:
                ESPACIO_TUBERIAS = 280;
                DISTANCIA_TUBERIAS = 350;
                break;
            case 4:
                ESPACIO_TUBERIAS = 260;
                DISTANCIA_TUBERIAS = 340;
                break;
            case 5:
                ESPACIO_TUBERIAS = 240;
                DISTANCIA_TUBERIAS = 330;
                break;
            case 6:
                ESPACIO_TUBERIAS = 220;
                DISTANCIA_TUBERIAS = 320;
                break;
            case 7:
                ESPACIO_TUBERIAS = 200;
                DISTANCIA_TUBERIAS = 310;
                break;
            case 8:
                ESPACIO_TUBERIAS = 180;
                DISTANCIA_TUBERIAS = 300;
                break;
            case 9:
                ESPACIO_TUBERIAS = 160;
                DISTANCIA_TUBERIAS = 290;
                break;
            case 10:
                ESPACIO_TUBERIAS = 140;
                DISTANCIA_TUBERIAS = 280;
                break;
            default:
                ESPACIO_TUBERIAS = 140;
                DISTANCIA_TUBERIAS = 280;
        }

        System.out.println("Nivel " + nivel + " - Espacio: " + ESPACIO_TUBERIAS + " - Distancia: " + DISTANCIA_TUBERIAS);
    }

    public void agregarTuberia() {
        int alturaMinima = 80; 
        int espacioMinimoInferior = 80; 
        
        int alturaMaximaSuperior = ALTO - ESPACIO_TUBERIAS - espacioMinimoInferior;
        
        if (alturaMinima > alturaMaximaSuperior) {
            alturaMinima = alturaMaximaSuperior / 2;
        }
        
        int alturaSuperior;
        if (alturaMaximaSuperior > alturaMinima) {
            alturaSuperior = new Random().nextInt(alturaMaximaSuperior - alturaMinima) + alturaMinima;
        } else {
            alturaSuperior = alturaMinima;
        }
        
        int alturaInferior = ALTO - (alturaSuperior + ESPACIO_TUBERIAS);
        
        if (alturaSuperior < 20 || alturaInferior < 20) {
            alturaSuperior = Math.max(alturaSuperior, 20);
            alturaInferior = Math.max(alturaInferior, 20);
            
            // Recalcular si es necesario
            if (alturaSuperior + ESPACIO_TUBERIAS + alturaInferior > ALTO) {
                // Ajustar la superior para que todo quepa
                alturaSuperior = ALTO - ESPACIO_TUBERIAS - 20;
                alturaInferior = 20;
            }
        }
        
        int id = siguienteIdTuberia++;

        // Crear tubería superior
        tuberias.add(new Tuberia(ANCHO, 0, ANCHO_TUBERIA, alturaSuperior, id, true));
        
        // Crear tubería inferior (asegurándose de que esté dentro de la pantalla)
        int yInferior = alturaSuperior + ESPACIO_TUBERIAS;
        tuberias.add(new Tuberia(ANCHO, yInferior, ANCHO_TUBERIA, alturaInferior, id, false));
        
        // Depuración: mostrar información de las tuberías creadas
        System.out.println("Tubería creada - ID: " + id + 
                        ", Superior: " + alturaSuperior + "px" +
                        ", Inferior: " + alturaInferior + "px" +
                        ", Espacio: " + ESPACIO_TUBERIAS + "px");
    }
    public void actualizar() {
        if (!juegoActivo || (modoEntrenamiento && entrenamientoPausado)) return;

        if (cambiandoNivel) {
            contadorCambioNivel--;
            if (contadorCambioNivel <= 0) {
                cambiandoNivel = false;
                tuberiasPasadasNivel = 0;
                actualizarDificultad();
            }
            repaint();
            return;
        }

        if (modoEntrenamiento) {
            actualizarEntrenamiento();
        } else {
            actualizarModoLibre();
        }

        verificarPuntuacion();
        repaint();
    }

    private void actualizarModoLibre() {
        pajaro.actualizar();
        actualizarTuberias();

        if (verificarColision(pajaro) || pajaro.y <= 0 || pajaro.y >= ALTO - 20) {
            perderVida();
        }
    }

    private void actualizarEntrenamiento() {
        boolean todosMuertos = true;

        for (Pajaro pajaro : poblacion) {
            if (pajaro.vivo) {
                todosMuertos = false;
                pajaro.actualizar();

                Tuberia tuberiaCercana = encontrarTuberiaCercana();
                if (tuberiaCercana != null) {
                    pajaro.pensar(tuberiaCercana);
                }

                if (verificarColision(pajaro) || pajaro.y <= 0 || pajaro.y >= ALTO - 20) {
                    pajaro.vivo = false;
                    pajaro.calcularAptitud();
                }
            }
        }

        actualizarTuberias();

        if (todosMuertos) {
            System.out.println("Generación " + generacion.generacion + " completada. Mejor puntuación: " + generacion.mejorRecord);
            generacion.seleccionNatural();
            actualizarUI();
            siguienteIdTuberia = 0;
            iniciarJuego();
        }
    }

    private Tuberia encontrarTuberiaCercana() {
        if (poblacion.isEmpty()) return null;

        for (Tuberia tuberia : tuberias) {
            if (tuberia.esSuperior && tuberia.x + tuberia.ancho > poblacion.get(0).x) {
                return tuberia;
            }
        }
        return tuberias.isEmpty() ? null : tuberias.get(0);
    }

    private void actualizarTuberias() {
        for (int i = 0; i < tuberias.size(); i++) {
            Tuberia tuberia = tuberias.get(i);
            tuberia.mover();

            if (tuberia.x + tuberia.ancho < 0) {
                tuberias.remove(i);
                i--;
            }
        }

        // Contar cuántas tuberías completas (pares de superior+inferior) tenemos
        Set<Integer> idsActivos = new HashSet<>();
        for (Tuberia tuberia : tuberias) {
            idsActivos.add(tuberia.id);
        }
        
        // Si no hay tuberías, agregar una
        if (idsActivos.isEmpty()) {
            agregarTuberia();
        }
        
        // Encontrar la tubería más a la derecha
        int maxX = -1;
        for (Tuberia tuberia : tuberias) {
            if (tuberia.x > maxX) {
                maxX = tuberia.x;
            }
        }
        
        // Agregar nueva tubería cuando la última esté suficientemente lejos
        if (maxX < ANCHO - DISTANCIA_TUBERIAS) {
            agregarTuberia();
        }
        
        // Actualizar scroll de la base
        baseScrollX1 -= VELOCIDAD_BASE;
        baseScrollX2 -= VELOCIDAD_BASE;
        
        // Reiniciar posición cuando la imagen sale completamente de la pantalla
        if (baseScrollX1 <= -imgBase.getWidth(this)) {
            baseScrollX1 = imgBase.getWidth(this) + baseScrollX2 - VELOCIDAD_BASE;
        }
        if (baseScrollX2 <= -imgBase.getWidth(this)) {
            baseScrollX2 = imgBase.getWidth(this) + baseScrollX1 - VELOCIDAD_BASE;
        }
    }

    private boolean verificarColision(Pajaro pajaro) {
        if (!pajaro.vivo) return false;

        for (Tuberia tuberia : tuberias) {
            if (tuberia.colisionaCon(pajaro)) {
                // Reproducir sonido de colisión
                reproducirSonidoColision();
                return true;
            }
        }
        return false;
    }

    private void verificarPuntuacion() {
        if (tuberias.isEmpty()) return;

        int referenciaX = modoEntrenamiento && !poblacion.isEmpty() ? poblacion.get(0).x : (pajaro != null ? pajaro.x : ANCHO / 4);

        for (Tuberia tuberia : tuberias) {
            if (tuberia.esSuperior && !tuberia.contada && tuberia.x + tuberia.ancho < referenciaX) {
                tuberia.contada = true;

                puntuacion++;
                tuberiasPasadasNivel++;

                if (puntuacion > mejorPuntuacion) {
                    mejorPuntuacion = puntuacion;
                }

                if (modoEntrenamiento) {
                    for (Pajaro p : poblacion) {
                        if (p.vivo) {
                            p.puntuacion = puntuacion;
                        }
                    }
                }

                if (tuberiasPasadasNivel >= TUBERIAS_POR_NIVEL) {
                    if (nivel < TOTAL_NIVELES) {
                        cambiarNivel();
                    } else {
                        completarEntrenamiento();
                    }
                }

                actualizarUI();
                break;
            }
        }
    }

    private void cambiarNivel() {
        nivel++;
        tuberiasPasadasNivel = 0;
        cambiandoNivel = true;
        contadorCambioNivel = 60;

        tuberias.clear();
        actualizarDificultad();
        actualizarUI();

        System.out.println("¡NIVEL " + nivel + " COMPLETADO!");
    }

    private void completarEntrenamiento() {
        entrenamientoCompletado = true;
        juegoActivo = false;
        timer.stop();

        // Guardar el estado actual antes de resetear
        if (generacion != null) {
            generacionAnterior = generacion.generacion;
            mejorRecordAnterior = generacion.mejorRecord;
            mejorFitnessAnterior = generacion.mejorFitness;
        }

        // Guardar los pájaros que completaron el entrenamiento (los que estaban vivos)
        poblacionCompletadores.clear();
        for (Pajaro pajaro : poblacion) {
            if (pajaro.vivo) {
                // Clonar los pájaros que sobrevivieron
                Pajaro clon = new Pajaro(pajaro.x, pajaro.y);
                clon.cerebro = pajaro.cerebro.clonar();
                clon.puntuacion = pajaro.puntuacion;
                clon.aptitud = pajaro.aptitud;
                poblacionCompletadores.add(clon);
            }
        }

        // Si no hay suficientes completadores, crear algunos basados en los mejores
        if (poblacionCompletadores.size() < 10) {
            // Ordenar la población original por fitness
            ArrayList<Pajaro> poblacionOrdenada = new ArrayList<>(poblacion);
            Collections.sort(poblacionOrdenada, (p1, p2) -> Double.compare(p2.aptitud, p1.aptitud));

            // Agregar los mejores hasta tener al menos 10
            for (int i = 0; i < Math.min(10, poblacionOrdenada.size()) && poblacionCompletadores.size() < 10; i++) {
                Pajaro clon = new Pajaro(poblacionOrdenada.get(i).x, poblacionOrdenada.get(i).y);
                clon.cerebro = poblacionOrdenada.get(i).cerebro.clonar();
                clon.puntuacion = poblacionOrdenada.get(i).puntuacion;
                clon.aptitud = poblacionOrdenada.get(i).aptitud;
                poblacionCompletadores.add(clon);
            }
        }

        btnIniciarML.setEnabled(false);
        btnPausarML.setEnabled(false);
        btnContinuarEntrenamiento.setEnabled(true);
        btnContinuarEntrenamiento.setVisible(true);

        System.out.println("¡ENTRENAMIENTO COMPLETADO! " +
                "Generación: " + generacionAnterior +
                ", Pájaros guardados: " + poblacionCompletadores.size());

        if (modoEntrenamiento) {
            JOptionPane.showMessageDialog(this,
                    "¡Entrenamiento Completado!\n" +
                            "Generación: " + generacionAnterior + "\n" +
                            "Puntuación final: " + puntuacion + "\n" +
                            "Nivel máximo: " + nivel + "\n" +
                            "Pájaros que completaron: " + poblacionCompletadores.size() + "\n" +
                            "Mejor Record: " + mejorRecordAnterior + "\n\n" +
                            "Puedes continuar entrenando con los pájaros exitosos\n" +
                            "manteniendo el progreso de generaciones.",
                    "Entrenamiento Completado", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    private void perderVida() {
        vidas--;
        actualizarUI();

        if (vidas <= 0) {
            // Reproducir sonido de muerte
            reproducirSonidoMuerte();
            terminarJuego();
        } else {
            pajaro = new Pajaro(ANCHO / 4, ALTO / 2);
            for (Tuberia tuberia : tuberias) {
                tuberia.contada = false;
            }
        }
    }

     public void terminarJuego() {
        juegoActivo = false;
        timer.stop();

        if (!modoEntrenamiento) {
            JOptionPane.showMessageDialog(this,
                    "¡Juego Terminado!\nPuntuación: " + puntuacion +
                            "\nNivel: " + nivel,
                    "Fin del Juego", JOptionPane.INFORMATION_MESSAGE);
        }
    }


    private void actualizarUI() {
        if (modoEntrenamiento && generacion != null) {
            lblGeneracion.setText("Generación: " + generacion.generacion);
            lblMejorFitness.setText("Mejor Fitness: " + String.format("%.2f", generacion.mejorFitness));
            lblMejorRecord.setText("Mejor Record: " + generacion.mejorRecord);

            ArrayList<Integer> topPuntuaciones = generacion.obtenerTopPuntuaciones(5);
            StringBuilder top5Text = new StringBuilder("<html>Top 5:<br>");
            for (int i = 0; i < topPuntuaciones.size(); i++) {
                top5Text.append(topPuntuaciones.get(i));
                if (i < topPuntuaciones.size() - 1) top5Text.append("<br>");
            }
            top5Text.append("</html>");
            lblTop5.setText(top5Text.toString());
        }

        lblPuntuacion.setText("Puntuación: " + puntuacion);
        lblMejorPuntuacion.setText("Mejor: " + mejorPuntuacion);
        lblVidas.setText("Vidas: " + vidas);
        lblNivel.setText("Nivel: " + nivel);
    }

     @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        if (!juegoActivo) {
            if (entrenamientoCompletado) {
                // Dibujar fondo
                dibujarFondo(g);
                
                // Capa semitransparente
                g.setColor(new Color(0, 0, 0, 128));
                g.fillRect(0, 0, ANCHO - 250, ALTO);

                g.setColor(Color.WHITE);
                g.setFont(new Font("Arial", Font.BOLD, 36));
                String mensaje = "¡ENTRENAMIENTO COMPLETADO!";
                int width = g.getFontMetrics().stringWidth(mensaje);
                g.drawString(mensaje, (ANCHO - 250 - width) / 2, ALTO / 2 - 50);

                g.setFont(new Font("Arial", Font.PLAIN, 18));
                String continuar = "Usa el botón 'Continuar Entrenando' para seguir";
                width = g.getFontMetrics().stringWidth(continuar);
                g.drawString(continuar, (ANCHO - 250 - width) / 2, ALTO / 2);

                String reiniciar = "o 'Reiniciar' para empezar desde el nivel 1";
                width = g.getFontMetrics().stringWidth(reiniciar);
                g.drawString(reiniciar, (ANCHO - 250 - width) / 2, ALTO / 2 + 30);
                
                // Dibujar base
                dibujarBase(g);
            }
            return;
        }

        // Dibujar fondo (alterna entre día y noche según el nivel)
        dibujarFondo(g);

        for (Tuberia tuberia : tuberias) {
            tuberia.dibujar(g);
        }

        if (modoEntrenamiento) {
            for (Pajaro pajaro : poblacion) {
                if (pajaro.vivo) {
                    pajaro.dibujar(g);
                }
            }
        } else if (pajaro != null) {
            pajaro.dibujar(g);
        }

        // Dibujar base
        dibujarBase(g);

        // Dibujar texto sobre la base (con fondo semitransparente para mejor legibilidad)
        g.setColor(new Color(0, 0, 0, 150));
        g.fillRect(10, 10, 200, 100);
        
        g.setColor(Color.WHITE);
        g.setFont(new Font("Arial", Font.BOLD, 24));
        g.drawString("Puntuación: " + puntuacion, 20, 30);
        g.drawString("Nivel: " + nivel, 20, 60);

        if (!modoEntrenamiento) {
            g.drawString("Vidas: " + vidas, 20, 90);
        } else {
            g.drawString("Vivos: " + contarPajarosVivos(), 20, 90);
            g.drawString("Gen: " + (generacion != null ? generacion.generacion : 0), 20, 120);
        }

        if (cambiandoNivel) {
            // Capa semitransparente sobre todo
            g.setColor(new Color(0, 0, 0, 180));
            g.fillRect(0, 0, ANCHO - 250, ALTO);

            g.setColor(Color.WHITE);
            g.setFont(new Font("Arial", Font.BOLD, 36));
            String mensaje = "NIVEL " + nivel;
            int width = g.getFontMetrics().stringWidth(mensaje);
            g.drawString(mensaje, (ANCHO - 250 - width) / 2, ALTO / 2);

            g.setFont(new Font("Arial", Font.PLAIN, 18));
            String continuar = "¡Nivel Completado!";
            width = g.getFontMetrics().stringWidth(continuar);
            g.drawString(continuar, (ANCHO - 250 - width) / 2, ALTO / 2 + 40);
        }
    }

    private int contarPajarosVivos() {
        if (poblacion == null) return 0;
        int count = 0;
        for (Pajaro pajaro : poblacion) {
            if (pajaro.vivo) count++;
        }
        return count;
    }
    private void dibujarFondo(Graphics g) {
        // Alternar entre fondo día y noche: niveles impares = día, niveles pares = noche
        Image fondoActual = (nivel % 2 == 1) ? imgFondoDia : imgFondoNoche;
        
        if (fondoActual != null) {
            // Dibujar la imagen del fondo escalada al tamaño de la ventana
            g.drawImage(fondoActual, 0, 0, ANCHO - 250, ALTO, this);
        } else {
            // Fallback en caso de que no se cargue la imagen
            Color colorFondo = (nivel % 2 == 1) ? 
                new Color(135, 206, 235) : // Azul cielo para día
                new Color(25, 25, 112);    // Azul noche para noche
            g.setColor(colorFondo);
            g.fillRect(0, 0, ANCHO - 250, ALTO);
        }
    }
    private void dibujarBase(Graphics g) {
        if (imgBase != null) {
            int alturaBase = 45; // Altura fija para la base
            
            // Dibujar la base en dos posiciones para scroll continuo
            g.drawImage(imgBase, baseScrollX1, ALTO - alturaBase, 
                       imgBase.getWidth(this), alturaBase, this);
            g.drawImage(imgBase, baseScrollX2, ALTO - alturaBase, 
                       imgBase.getWidth(this), alturaBase, this);
        } else {
            // Fallback: dibujar base sólida
            g.setColor(new Color(222, 184, 135));
            g.fillRect(0, ALTO - 100, ANCHO - 250, 100);
        }
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        actualizar();
    }

    @Override
    public void keyPressed(KeyEvent e) {
        if (e.getKeyCode() == KeyEvent.VK_SPACE && juegoActivo && !modoEntrenamiento && pajaro != null) {
            pajaro.saltar();
            // Reproducir sonido de salto
            reproducirSonidoSalto();
        }
    }

    @Override
    public void keyReleased(KeyEvent e) {}

    @Override
    public void keyTyped(KeyEvent e) {}

    public static void main(String[] args) {
        JFrame frame = new JFrame("Flappy Bird - Machine Learning");
        JuegoPrincipal juego = new JuegoPrincipal();

        frame.add(juego);
        frame.pack();
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);

        juego.requestFocusInWindow();
        
        // Añadir listener para cerrar sonidos cuando se cierre la ventana
        frame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                juego.cerrarSonidos();
            }
        });
    }
}