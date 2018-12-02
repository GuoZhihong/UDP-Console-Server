package Others;

import UDP.Packet;

import java.util.Queue;

public class Window {
    private final int windowSIZE = 4;
    private Queue<Packet> window;

    public void add(Packet packet){
        this.window.offer(packet);
    }

    public void delete(){
        this.window.poll();
    }

    public void deleteAll(){
        this.window.clear();
    }
    public boolean isFull(){
        if(this.window.size() < this.windowSIZE){
            return true;
        }
        return false;
    }

    public Packet peek(){
        return this.window.peek();
    }
}
