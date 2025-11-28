package FlappyBird;

import java.util.*;

public class Generacion {
    public ArrayList<Pajaro> poblacion;
    public int generacion;
    public double mejorFitness;
    public int mejorRecord;
    public double tasaMutacion;
    public double promedioFitness;
    public int tamañoPoblacion;
    private ArrayList<Integer> historialPuntuaciones;

    // Constructor para nueva generación normal
    public Generacion(int tamaño) {
        tamañoPoblacion = tamaño;
        poblacion = new ArrayList<>();
        historialPuntuaciones = new ArrayList<>();
        for (int i = 0; i < tamaño; i++) {
            poblacion.add(new Pajaro(200, 350));
        }
        generacion = 1;
        mejorFitness = 0;
        mejorRecord = 0;
        tasaMutacion = 0.1;
        promedioFitness = 0;
    }

    // Constructor para continuar entrenamiento con completadores y mantener progreso
    public Generacion(int tamaño, ArrayList<Pajaro> completadores, int generacionAnterior, int mejorRecordAnterior, double mejorFitnessAnterior) {
        tamañoPoblacion = tamaño;
        poblacion = new ArrayList<>();
        historialPuntuaciones = new ArrayList<>();

        // MANTENER EL PROGRESO DE GENERACIONES Y RECORDS
        this.generacion = generacionAnterior + 1; // Continuar desde la siguiente generación
        this.mejorRecord = mejorRecordAnterior;
        this.mejorFitness = mejorFitnessAnterior;

        if (completadores == null || completadores.isEmpty()) {
            // Si no hay completadores, crear generación normal
            for (int i = 0; i < tamaño; i++) {
                poblacion.add(new Pajaro(200, 350));
            }
        } else {
            // Usar los completadores como base para la nueva población
            Random rand = new Random();

            // ELITE: 40% de la población son los completadores originales o sus clones
            int eliteCount = Math.min(completadores.size(), tamaño * 40 / 100);
            for (int i = 0; i < eliteCount; i++) {
                Pajaro elite = new Pajaro(200, 350);
                elite.cerebro = completadores.get(i % completadores.size()).cerebro.clonar();
                poblacion.add(elite);
            }

            // 30% son hijos de completadores entre sí
            int hijosCount = tamaño * 30 / 100;
            for (int i = 0; i < hijosCount && poblacion.size() < tamaño; i++) {
                int idx1 = rand.nextInt(completadores.size());
                int idx2 = rand.nextInt(completadores.size());

                Pajaro hijo = completadores.get(idx1).cruzar(completadores.get(idx2));
                hijo.mutar(tasaMutacion * 0.5); // Mutación suave
                poblacion.add(hijo);
            }

            // 20% son mutaciones de los mejores completadores
            int mutantesCount = tamaño * 20 / 100;
            for (int i = 0; i < mutantesCount && poblacion.size() < tamaño; i++) {
                int idx = rand.nextInt(Math.min(5, completadores.size()));
                Pajaro mutante = new Pajaro(200, 350);
                mutante.cerebro = completadores.get(idx).cerebro.clonar();
                mutante.mutar(tasaMutacion * 0.8); // Mutación media
                poblacion.add(mutante);
            }

            // 10% son nuevos aleatorios para diversidad
            while (poblacion.size() < tamaño) {
                poblacion.add(new Pajaro(200, 350));
            }
        }

        tasaMutacion = 0.05; // Mutación más baja para entrenamiento avanzado
        promedioFitness = 0;

        System.out.println("Continuando en Generación: " + this.generacion +
                " - Mejor Record: " + this.mejorRecord +
                " - Pájaros base: " + (completadores != null ? completadores.size() : 0));
    }

    public void seleccionNatural() {
        System.out.println("=== GENERACIÓN " + generacion + " ===");

        // Calcular fitness para todos los pájaros
        double sumaFitness = 0;
        int maxPuntos = 0;

        for (Pajaro pajaro : poblacion) {
            pajaro.calcularAptitud();
            sumaFitness += pajaro.aptitud;
            maxPuntos = Math.max(maxPuntos, pajaro.puntuacion);
            historialPuntuaciones.add(pajaro.puntuacion);
        }

        // Ordenar por fitness (mejores primero)
        Collections.sort(poblacion, (p1, p2) -> Double.compare(p2.aptitud, p1.aptitud));

        // Actualizar estadísticas (mantener el mejor record histórico)
        mejorFitness = Math.max(mejorFitness, poblacion.get(0).aptitud);
        mejorRecord = Math.max(mejorRecord, poblacion.get(0).puntuacion);
        promedioFitness = sumaFitness / poblacion.size();

        System.out.println("Mejor esta gen: " + poblacion.get(0).puntuacion + " tubos, Fitness: " +
                String.format("%.0f", poblacion.get(0).aptitud));
        System.out.println("Mejor histórico: " + mejorRecord + " tubos");
        System.out.println("Promedio: " + String.format("%.0f", promedioFitness));
        System.out.println("Máximo esta gen: " + maxPuntos);

        // Estrategia de mutación más conservadora para entrenamiento avanzado
        if (generacion < 5) {
            tasaMutacion = 0.08;
        } else if (mejorRecord < 10) {
            tasaMutacion = 0.06;
        } else {
            tasaMutacion = 0.04; // Mutación muy baja para expertos
        }

        // Crear nueva población
        ArrayList<Pajaro> nuevaPoblacion = new ArrayList<>();
        Random rand = new Random();

        // ELITE: 30% mejores pasan directamente
        int elite = Math.max(10, poblacion.size() / 3);
        for (int i = 0; i < elite; i++) {
            Pajaro elitePajaro = new Pajaro(200, 350);
            elitePajaro.cerebro = poblacion.get(i).cerebro.clonar();
            nuevaPoblacion.add(elitePajaro);
        }

        // 50% por cruce de los mejores 50%
        while (nuevaPoblacion.size() < poblacion.size() * 0.8) {
            int idx1 = rand.nextInt(poblacion.size() / 2);
            int idx2 = rand.nextInt(poblacion.size() / 2);

            Pajaro hijo = poblacion.get(idx1).cruzar(poblacion.get(idx2));
            hijo.mutar(tasaMutacion);
            nuevaPoblacion.add(hijo);
        }

        // 20% nuevos aleatorios
        while (nuevaPoblacion.size() < poblacion.size()) {
            nuevaPoblacion.add(new Pajaro(200, 350));
        }

        poblacion = nuevaPoblacion;
        generacion++; // Incrementar la generación

        System.out.println("Próxima generación: " + generacion);
    }

    public ArrayList<Integer> obtenerTopPuntuaciones(int n) {
        ArrayList<Integer> top = new ArrayList<>();
        for (int i = 0; i < Math.min(n, poblacion.size()); i++) {
            top.add(poblacion.get(i).puntuacion);
        }
        Collections.sort(top, Collections.reverseOrder());

        while (top.size() < n) {
            top.add(0);
        }
        return top;
    }

    public boolean todosMuertos() {
        for (Pajaro pajaro : poblacion) {
            if (pajaro.vivo) return false;
        }
        return true;
    }
}
