package FlappyBird;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.Random;
import java.util.HashSet;
import java.util.Set;
import java.util.Collections;
import java.util.Comparator;

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

    // Solo 2 colores que alternan por nivel
    private Color[] coloresFondo = {
            new Color(135, 206, 235), // Azul cielo
            new Color(169, 169, 169)  // Gris
    };

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

    public JuegoPrincipal() {
        setPreferredSize(new Dimension(ANCHO, ALTO));
        setBackground(coloresFondo[0]);
        setLayout(new BorderLayout());

        timer = new Timer(16, this);
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

        crearPanelControl();
        addKeyListener(this);
        setFocusable(true);

        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                requestFocusInWindow();
            }
        });
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
        int alturaMinima = 50;
        int alturaMaxima = ALTO - ESPACIO_TUBERIAS - alturaMinima;
        int alturaSuperior = new Random().nextInt(alturaMaxima - alturaMinima) + alturaMinima;

        int id = siguienteIdTuberia++;

        tuberias.add(new Tuberia(ANCHO, 0, ANCHO_TUBERIA, alturaSuperior, id, true));
        tuberias.add(new Tuberia(ANCHO, alturaSuperior + ESPACIO_TUBERIAS,
                ANCHO_TUBERIA, ALTO - alturaSuperior - ESPACIO_TUBERIAS, id, false));
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

        if (tuberias.isEmpty() || tuberias.get(tuberias.size() - 1).x < ANCHO - DISTANCIA_TUBERIAS) {
            agregarTuberia();
        }
    }

    private boolean verificarColision(Pajaro pajaro) {
        if (!pajaro.vivo) return false;

        for (Tuberia tuberia : tuberias) {
            if (tuberia.colisionaCon(pajaro)) {
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
                // Mostrar mensaje de entrenamiento completado
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
            }
            return;
        }

        // Alternar entre azul cielo y gris por nivel (nivel impar: azul, nivel par: gris)
        Color colorFondo = coloresFondo[(nivel - 1) % 2];
        g.setColor(colorFondo);
        g.fillRect(0, 0, ANCHO - 250, ALTO);

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

        g.setColor(Color.BLACK);
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
            g.setColor(new Color(0, 0, 0, 128));
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

    @Override
    public void actionPerformed(ActionEvent e) {
        actualizar();
    }

    @Override
    public void keyPressed(KeyEvent e) {
        if (e.getKeyCode() == KeyEvent.VK_SPACE && juegoActivo && !modoEntrenamiento && pajaro != null) {
            pajaro.saltar();
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
    }
}

