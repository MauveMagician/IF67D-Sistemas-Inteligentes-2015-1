package br.edu.utfpr.hexgrid;

import java.io.Serializable;
import java.util.ArrayDeque;
import java.util.ArrayList;

/**
 *
 * @author Bernardo
 * Inspirado por: http://www.redblobgames.com/grids/hexagons/
 */
public class HexBoard implements Serializable {
    ArrayList<ArrayList<Hex>> board;

    public ArrayList<ArrayList<Hex>> getBoard() {
        return board;
    }

    public void setBoard(ArrayList<ArrayList<Hex>> board) {
        this.board = board;
    }
    
    public HexBoard(ArrayList<Pos> aliados, ArrayList<Pos> inimigos, Pos objetivo, int raiobarreira){
        //instanciar os vertices
        board = new ArrayList();
        for(int i = 0; i < 41; i++)
            board.add(new ArrayList());
        
        for(int i = 0; i < 41; i++)        
            for(int j = 0; j < 25; j++)
                    board.get(i).add(new Hex(i, j));
        
        Hex goal = new Hex(objetivo.getX(),objetivo.getY());
       
       //Heuristica utilizada: distancia entre dois pontos
       for(int i = 0; i < 41; i++){
           for(int j = 0; j < 25; j++){
               board.get(i).get(j).setH((int) Math.sqrt((Math.abs(goal.getX()-i) + Math.abs(goal.getY() - j)) * 60));
           }
        }
       
        for(int i = 0; i < 41; i++)        
            for(int j = 0; j < 25; j++){
               if((j % 2) > 0){
                   if(j > 0)
                        board.get(i).get(j).getVizinhos().add(board.get(i).get(j-1));
                   if(i < 40 && j > 0)
                        board.get(i).get(j).getVizinhos().add(board.get(i+1).get(j-1));
                   if(i > 0)                   
                        board.get(i).get(j).getVizinhos().add(board.get(i-1).get(j));
                   if(j < 24)
                        board.get(i).get(j).getVizinhos().add(board.get(i).get(j+1));
                   if(i < 40 && j < 24)
                        board.get(i).get(j).getVizinhos().add(board.get(i+1).get(j+1));
                   if(i < 40)
                        board.get(i).get(j).getVizinhos().add(board.get(i+1).get(j));                   
               }
               else{
                   if(i > 0 && j > 0)  
                        board.get(i).get(j).getVizinhos().add(board.get(i-1).get(j-1));
                   if(j > 0)
                        board.get(i).get(j).getVizinhos().add(board.get(i).get(j-1));
                   if(i < 40)
                        board.get(i).get(j).getVizinhos().add(board.get(i+1).get(j));
                   if(j < 24)
                        board.get(i).get(j).getVizinhos().add(board.get(i).get(j+1));
                   if(i > 0 && j < 24)
                        board.get(i).get(j).getVizinhos().add(board.get(i-1).get(j+1));
                   if(i > 0)
                        board.get(i).get(j).getVizinhos().add(board.get(i-1).get(j));
            }
        }       
       
       //TODO marcar as células de barreira como bloqueadas
       this.barreira(aliados, inimigos, raiobarreira);
       
       
        
    }
    
    private void barreira(ArrayList<Pos> aliados, ArrayList<Pos> inimigos, int raiobarreira){
        for(Pos p : aliados){
            this.board.get(p.x).get(p.y).setBlock(true);
        }
        for(Pos p : inimigos){
            this.board.get(p.x).get(p.y).setBlock(true);
            ArrayList<Hex> doBlock = this.board.get(p.x).get(p.y).getVizinhos();
            for(int i = 0; i < raiobarreira; i++){
                ArrayList<Hex> toAdd = new ArrayList();
                for(Hex h: doBlock){
                    h.setBlock(true);
                    toAdd.addAll(h.getVizinhos());
                }
                doBlock.clear();
                doBlock.addAll(toAdd);
            }
        }
    }
    
    public ArrayDeque<Pos> Astar(int sx, int sy, int gx, int gy){
        ArrayDeque<Pos> plano = new ArrayDeque();
        ArrayList<Hex> closed = new ArrayList();
        Hex current = board.get(sx).get(sy);
        ArrayList<Hex> open = new ArrayList();
        open.add(current);        
        
        while(!open.isEmpty()){         
            Hex menorf = open.get(0);
            for(Hex h: open){
                if(h.getF() < menorf.getF()){
                    menorf = h;
                }
            }
            current = menorf;
            open.remove(current);
            if(open.isEmpty()){
                System.out.println("ESVAZIOU O OPEN!!!!!!");
            }
            closed.add(current);
            if(current.getX() == gx && current.getY() == gy){
                System.out.println("A* CHEGOU EM GOAL!!!!");
                while(current.getX() != sx || current.getY() != sy){
                    //System.out.println(current.getX() + "," + current.getY());
                    plano.push(new Pos(current.getX(), current.getY()));
                    current = current.getParent();
                }
                return plano;
            }
            for(Hex v: current.getVizinhos()){
                if(!closed.contains(v) && !v.isBlock()){
                    if(!open.contains(v)){
                        open.add(v);
                        v.setParent(current);
                        v.setG(current.getG() + 1);
                    }
                    else if(current.getG() + 1 < v.getF() ){
                            v.setG(current.getG() + 1);
                            v.setParent(current);
                    }
                }
            }
        }
        return new ArrayDeque<>();
    }
    
    //TODO ler do arquivo
    public Pos LRTAstar(int sx, int sy, int gx, int gy){
        Hex current = board.get(sx).get(sy);
        Hex last = current;
        current.setInflacao(current.getInflacao() + 60);
        for(Hex v: current.getVizinhos()){
            v.setInflacao(v.getInflacao() + 60);
        }
        Hex menorf = current.getVizinhos().get(0);
        for(Hex w: current.getVizinhos()){
            if(w.getH() + w.getInflacao() < menorf.getH() + menorf.getInflacao()){
                menorf = w;
            }
            else if(w.getH() + w.getInflacao() == menorf.getH() + menorf.getInflacao()){
                double r =  Math.random();
                if(r > 0.5){
                    menorf = w;
                }
            }
        }
        if(menorf.getX() == gx && menorf.getY() == gy)
            return new Pos(-1, -1);
        return new Pos(menorf.getX() * 3600, menorf.getY() * 3600);
    }
    
}
