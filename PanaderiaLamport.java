/* ###################################### ALGORITMO DE LA PANADERIA DE LAMPORT ################################################ */
public class Bakery extends Thread {

  //CREAMOS VARIABLES PARA LOS DIFERENTES HILOS
  public int thread_id; //EL ID DEL HILO
  public static final int countToThis = 20; //TIEMPO ESTIMADO POR PROCESO
  public static final int numberOfThreads = 5; //NUMERO DE HILOS
  public static volatile int count = 0; //CREAMOS CONTADOR PARA EL TESTEO DEL PROGRAMA

  //VARIABLES GLOBALES PARA EL ALGORITMO DE LAMPORT (O DEL PANADERO)
  private static volatile boolean[] choosing = new boolean[numberOfThreads]; //ARRAY DONDE ESTÁN CONTENIDOS LOS VALORES BOOLEANOS PARA CADA HILO Y MUESTRA SI EL HILO DESEA ENTRAR EN LA ZONA CRITICA O NO
  private static volatile int[] ticket = new int[numberOfThreads]; //EL TICKET SE CREA PARA DEFINIR UNA PRIORIDAD

  /*
   * CONSTRUCTOR DE HILOS:
   */
  public Bakery(int id) {
    thread_id = id;
  }

  //Metodo runnable para la ejecucion de los hilos al solicitar acceso a la seccion critica
  public void run() {
    int scale = 2;

    for (int i = 0; i < countToThis; i++) {

      lock(thread_id);//Cierra el cerrojo
      //INICIO DE LA SECCIÓN CRITICA
      count = count + 1;
      System.out.println("I am " + thread_id + " and count is: " + count);
      //ESPERA PARA LA CREACIÓN DE UNA "CONDICION DE CARRERA" ENTRE LOS HILOS
      try {
        sleep((int) (Math.random() * scale));
      } catch (InterruptedException e) { /* NADA */ }
      //FIN DE LA SECCION CRITICA
      unlock(thread_id);//Abre el cerrojo
    }
  }


  /*
   * Metodo para hacer de cerrojo en la seccion critica del algoritmo de Lamport.Con ello producimos la exclusion mutua entre los hilos
   */
  public void lock(int id) {
    // Esto quiere decir que el hilo (con su id = id) desea acceder a la seccion critica
    choosing[id] = true;

    // Encuentra el valor maximo y le suma 1 para obtener el siguiente ticket disponible
    ticket[id] = findMax() + 1;
    choosing[id] = false;

    // Mensaje para indicar cuando un hilo ha accedido al lock procediendo a su cierre
    System.out.println("Thread " + id + " got ticket in Lock");

    for (int j = 0; j < numberOfThreads; j++) {

      // Si el valor de j coincide con el hilo actual, continua al siguiente hilo
      if (j == id)
        continue;

      // Sistema para generar la espera si el hilo j esta eligiendo en ese momento
      while (choosing[j]) { /* no hace nada */ }

      // Sistema que genera el bloqueo usando el cerrojo e inahibilitando el acceso a la seccion critica
      while (ticket[j] != 0 && (ticket[id] > ticket[j] || (ticket[id] == ticket[j] && id > j))) { /* no hace nada */ }

    }
  }

  /*
   * Metodo que libre el cerrojo y vuelve a permitir el acceso a la seccion critica al resto de hilos
   */
  private void unlock(int id) {
    ticket[id] = 0;

    // Mensaje para indicar que hilo ha dejado libre la seccion critica y por tanto abierto el lock
    System.out.println("Thread " + id + " unlock");
  }

  /*
   * Metodo que encuentra el maximo valor dentro del array de tickets
   */
  private int findMax() {

    int m = ticket[0];
    // For que recorre el array de tickets y va actualizando su valor maximo hasta recorrerlo por completo
    for (int i = 1; i < ticket.length; i++) {
      if (ticket[i] > m)
        m = ticket[i];
    }
    return m;
  }

  public static void main(String[] args) {

    //INICIALIZACION DE LAS VARIABLES GLOBALES
    for (int i = 0; i < numberOfThreads; i++) {
      choosing[i] = false;
      ticket[i] = 0;
    }

    Bakery[] threads = new Bakery[numberOfThreads]; //ARRAY DE HILOS

    //INICIALIZACION DE LOS HILOS
    for (int i = 0; i < threads.length; i++) {
      threads[i] = new Bakery(i);
      threads[i].start();//El metodo start es el que hace da la señal de inicio a los hilos
    }

    //ESPERA A TODOS LOS HILOS A QUE TERMINEN
    for (int i = 0; i < threads.length; i++) {
      try {
        threads[i].join();
      } catch (InterruptedException e) {
        e.printStackTrace();
      }
    }
    System.out.println("\nCount is: " + count);
    System.out.println("\nExpected was: " + (countToThis * numberOfThreads));
  }

}