package Criteriosalgoritmos;

/**
 *
 * Clase de Union Find, mantiene la información de los distintos subconjuntos disjuntos de un conjunto,
 *
 * Tiene 2 atributos
 * ID: guarda el identificador de cada subconjunto disjunto (el padre del arbol que representa a ese subconjunto)
 * SIZE: guarda el tamaño del árbol que representa a cada conjunto disjunto
 */
public class UnionFind {
    private int[] id;
    private int[] size;

    /**
     * Constructora de UnionFind, inicializa cada elemento id[] con su propia posicion en el array
     * @param n
     */
    public UnionFind(int n) {
        id = new int[n];
        size = new int[n];
        for (int i = 0; i < n; i++) {
            id[i] = i;
            size[i] = 1;
        }
    }

    /**
     * Find eficiente, sirve para saber en que subconjunto disjunto pertenece el elemento x
     * usamos compresion de caminos para que sea mas eficiente
     * @param x
     * @return
     */
    public int find(int x) {
        if (x != id[x]) {
            id[x] = find(id[x]); //voy a la raíz del árbol
            x = id[x]; //compresión de caminos
        }
        return x;
    }

    /**
     * Union eficiente, sirve para unir los subconjuntos disjuntos de x e de y (si pertenecen a subconjuntos distintos)
     * tenemos en cuenta el tamano de cada subconjunto para que sea mas eficiente
     * @param x
     * @param y
     */
    public void union(int x, int y) {
        int rootX = find(x);
        int rootY = find(y);
        if (rootX != rootY) {
            //cuelgo el "arbol" pequeño al "arbol" grande (un arbol representa a una componente conexa)
            if (size[rootX] > size[rootY]) {
                id[rootY] = rootX;
                size[rootX] += size[rootY];
            } else {
                id[rootX] = rootY;
                size[rootY] += size[rootX];
            }
        }
    }

    public UnionFind clone() {
        UnionFind copy = new UnionFind(id.length);
        copy.id = id.clone();
        copy.size = size.clone();
        return copy;

    }

}