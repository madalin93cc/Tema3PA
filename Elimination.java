
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author Colezea
 */
public class Elimination {
    // declaratii de variabile
    private final byte N;
    private final byte M;
    private final byte K;
    private final byte R; 
    private final byte[][] nava; // structura initiala a navei
    private final ArrayList<Bomba> bombe; // lista cu toate seturile de bombe
    private long cost_min; // costul minim ce va fi calculat
    private ArrayList<Integer> miscari; // indicii detonarilor ce vor fi facute
    
    // clasa ce retine un set de bombee ca o lista de perechi de indici
    class Bomba{
        ArrayList<Pair> camere;
        int cost;
        public Bomba(int cost, int nr){
            camere = new ArrayList<>(nr);
            this.cost = cost;
        }
        public String toString(){
            return camere.toString() + "\n" + cost;
        }
    }
    
    // clasa ce retine o pereche de indici
    class Pair{
        int x,y;
        public Pair(int x, int y){
            this.x = x;
            this.y = y;
        }
        public String toString(){
            return "( " + x + "," + y + " )";
        }
    }
    
    // constructorul
    public Elimination(String filename) throws FileNotFoundException, IOException{
        BufferedReader bufferin = new BufferedReader(new FileReader(filename));
        String[] data = bufferin.readLine().split(" ");
        N = Byte.parseByte(data[0]);
        M = Byte.parseByte(data[1]);
        K = Byte.parseByte(data[2]);
        R = Byte.parseByte(data[3]);
        cost_min = Integer.MAX_VALUE; // costul initial maxim
        miscari = new ArrayList(); // nu s-au efectuat miscari
        nava = new byte[N][M]; 
        bombe = new ArrayList<>(R);
        // adaug inamicii in structura iniaiala a navei dupa ce ii citesc din fisier
        for (byte i = 0; i < K; i++){
            data = bufferin.readLine().split(" ");
            byte x = Byte.parseByte(data[0]);
            byte y = Byte.parseByte(data[1]);
            nava[x][y] = (byte) (i + 1);
        }
        // adaug bombele in lista de seturi de bombe
        for (byte i = 0; i < R; i++){
            data = bufferin.readLine().split(" ");
            int cost = Integer.parseInt(data[0]);
            byte nr = Byte.parseByte(data[1]);
            Bomba b = new Bomba(cost, nr);
            int k = 2;
            for (int j = 0; j < nr; j++){
                int x = Byte.parseByte(data[k++]);
                int y = Byte.parseByte(data[k++]);
                b.camere.add(new Pair(x,y));
            }
            bombe.add(b);
        }
        
        explore(nava, 0, new ArrayList(), new ArrayList<byte[][]>());
        //scriere in fisier
        BufferedWriter bufferout = new BufferedWriter(new FileWriter("elimination.out"));
        bufferout.write(cost_min+"\n"); // costul
        bufferout.write(miscari.size()+"\n"); // numarul de pasi
        for(int i:miscari){ 
            bufferout.write(i+"\n"); // indicii seturilor
        }
        bufferin.close();
        bufferout.close();
    }
    public void explore(final byte[][] nava_init, long cost, ArrayList<Integer> m, ArrayList<byte[][]> istoric){
        // pentru a evita recurentele evit explorarea e 2 ori a unei stari
        if (contine(istoric, nava_init) == true){
           return; 
        }
        else { // adaug in stari explorate daca nu exista deja 
            istoric.add(nava_init);
        }
        // daca oricum as obtine un scor mai mare ma opresc din recurenta
        if (cost > cost_min) {
            return;
        }
        // explorez starile obtinute prin detonarea pe rand a fiecarui set de bombe
        for (int i = 0; i < bombe.size(); i++){
            Bomba b = bombe.get(i);
            ArrayList<byte[][]>a = new ArrayList<>();
            // costul actual + costul detonarii
            long newcost = cost + b.cost;
            // pentru a nu explora detonari inutil verific daca obtin un cost mai mare
            if (newcost > cost_min) continue;
            byte[][] x = new byte[N][M];
            for (byte p = 0; p < N; p++){
                for (byte q = 0; q < M; q++){
                    x[p][q] = nava_init[p][q];
                }
            }
            // nava obtinuta un urma detonarii
            byte[][] nava_noua = move(x, b);
            if (allDead(nava_noua)){ // daca am eliminat toti inamicii
                m.add(i+1); // actualizez vectorul de detonari
                if (newcost < cost_min){ // daca am un cost mai bun retin noua solutie
                    cost_min = newcost;
                    miscari = new ArrayList(m);
                }
            }
            else { // apelez recursiv metoda explore 
                ArrayList<Integer> m_nou = new ArrayList<>(m);
                m_nou.add(i+1);
                ArrayList<byte[][]> is_nou = new ArrayList<>(istoric);
                explore(nava_noua, newcost, m_nou, is_nou);
            }
        }
    }
    // metoda ce verifica daca vectorul de stari contine deja o stare data ca parametru
    boolean contine(ArrayList<byte[][]> a, byte[][]b){
        for (byte[][]x:a){
            if (comp(x,b)){
                return true;
            }
        }
        return false;
    }
    // compara doua stari(nave)
    boolean comp(byte[][]a, byte[][]b){
        for (int i = 0; i < N; i++){
            for (int j = 0; j< M; j++){
                if (a[i][j] != b[i][j]){
                    return false;
                }
            }
        }
        return true;
    }
    // copiaza doua stari(nave)
    byte[][] copym(byte[][] n){
        byte[][] y = new byte[N][M];
        for (byte p = 0; p < N; p++){
            for (byte q = 0; q < M; q++){
                y[p][q] = n[p][q];
            }
        }
        return y;
    }
    // verifica daca nu mai sunt inamici in nava
    boolean allDead(byte[][] n){
        for (int i = 0; i < N; i++){
            for (int j = 0; j < M; j++){
                if (n[i][j] != 0){
                    return false;
                }
            }
        }
        return true;
    }
    // returneaza nava obtinta in urma unei detonari
    byte[][] move(byte[][] n, Bomba b){
        byte[][] nnew = new byte[N][M];
        for (Pair p:b.camere){
            n[p.x][p.y] = 0;
        }
        for (int i = 0; i < N; i++){
            for(int j = 0; j < M; j++){
                if (n[i][j] != 0){
                    if (i - 1 >= 0)
                        nnew[i-1][j] = n[i][j];
                    if (i + 1 < N)
                        nnew[i+1][j] = n[i][j];
                    if (j - 1 >= 0)
                        nnew[i][j-1] = n[i][j];
                    if (j + 1 < M)
                        nnew[i][j+1] = n[i][j];
                }
            }
        }
        return nnew;
    }
    public static void main(String args[]) throws FileNotFoundException, IOException{
        Elimination e = new Elimination("elimination.in");
    }
}
