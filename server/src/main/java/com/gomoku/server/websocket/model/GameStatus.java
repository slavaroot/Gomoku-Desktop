package com.gomoku.server.websocket.model;

import com.gomoku.server.mongo.model.Match;
import com.gomoku.server.mongo.repository.MatchRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class GameStatus {

    private WebSocketSession master;
    private WebSocketSession guest;
    private Set<WebSocketSession> audience;

    private String masterName;
    private String guestName;

    private int masterStone;
    private int guestStone;

    private boolean guestReady;

    // game logic, store the board and stones
    // check whether a player wins and put the stone correctly
    private GameLogic gameLogic;

    private List<TextMessage> historyMoves;

    public void addAudience(WebSocketSession audienceSession){
        if(master != audienceSession || guest != audienceSession){
            audience.add(audienceSession);
        }
    }

    // put a stone, and give the message should be send
    // add winFlag
    public TextMessage move(int player, TextMessage message) throws Exception {
        if(! ready()){
            throw new Exception("Game not starts yet.");
        }
        int pos = Integer.parseInt(message.getPayload());
        if (pos < 0){
            throw new Exception("Control signal, not moving signal.");
        }
        // move() return a int, which contains all info, with winFlag
        TextMessage toSend = new TextMessage(this.gameLogic.move(player, pos) + "");

        // save every move, send to late audience
        this.appendHistory(toSend);

        return toSend;
    }

    // put a stone, and give the message should be send
    // add winFlag
    public TextMessage move(int player, int pos) throws Exception {
        if(! ready()){
            throw new Exception("Game not start yet.");
        }
        if (pos < 0){
            throw new Exception("Control signal, not moving signal.");
        }
        // move() return a int, which contains all info, with winFlag
        TextMessage toSend = new TextMessage(this.gameLogic.move(player, pos) + "");

        // save every move, send to late audience
        this.appendHistory(toSend);

        return toSend;
    }

    public void surrender(int role){
        this.gameLogic.surrender(role);
    }

    private void appendHistory(TextMessage move){
        this.historyMoves.add(move);
    }

    // send all past moves to a player session, audience usually
    public void sendAllMoves(WebSocketSession audience){
        this.historyMoves.forEach(m -> {
            try {
                audience.sendMessage(m);
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }

    public void setMasterInfo(String masterName, WebSocketSession masterSession){
        this.masterName = masterName;
        this.master = masterSession;
    }

    public int getStone(String role) throws Exception{
        if(role.equals("m")){
            return this.masterStone;
        }else if(role.equals("g")){
            return this.guestStone;
        }else{
            throw new Exception("Invalid role.");
        }
    }

    public void setGuestInfo(String guestName, WebSocketSession guestSession){
        this.guestName = guestName;
        this.guest = guestSession;
    }

    public int nextMove(){
        return this.gameLogic.getRound();
    }

    // constructor
    public GameStatus(int masterStone) throws Exception{
        this.gameLogic = new GameLogic();
        this.audience = new HashSet<>();
        this.guestReady = false;
        this.historyMoves = new ArrayList<>();
        if(masterStone == 1){
            this.masterStone = 1;
            this.guestStone = 2;
        }else if(masterStone == 2){
            this.masterStone = 2;
            this.guestStone = 1;
        }else{
            throw new Exception("Invalid stones setting.");
        }
    }

    // constructor
    public GameStatus(int masterStone, String masterName, WebSocketSession masterSession) throws Exception{
        this.gameLogic = new GameLogic();
        this.audience = new HashSet<>();
        this.guestReady = false;
        this.historyMoves = new ArrayList<>();
        if(masterStone == 1){
            this.masterStone = 1;
            this.guestStone = 2;
        }else if(masterStone == 2){
            this.masterStone = 2;
            this.guestStone = 1;
        }else{
            throw new Exception("Invalid stones setting.");
        }
        setMasterInfo(masterName, masterSession);
    }

    // testing info
    public void test(){
        System.out.println(this.masterName+" $ "+this.guestName+" $ ");
        audience.forEach(ele->{System.out.println(ele);});
    }

    // check whether the master and guest session are set
    public boolean ready(){
        return (this.master!=null && this.guest!=null && this.masterName!=null && this.guestName!=null && this.guestReady);
    }

    // send game start signal to master and guest
    // Important: moved this function to GameSocketHandler
//    public void start() throws Exception {
//        if (! ready()){
//            throw new Exception("Someone unready.");
//        }
//        this.master.sendMessage(new TextMessage("masterstart"));
//        this.guest.sendMessage(new TextMessage("gueststart"));
//
//    }

    public WebSocketSession getMaster() {
        return master;
    }


    public WebSocketSession getGuest() {
        return guest;
    }

    public Set<WebSocketSession> getAudience() {
        return audience;
    }

    public String getMasterName() {
        return masterName;
    }

    public String getGuestName() {
        return guestName;
    }

    public GameLogic getGameLogic() {
        return gameLogic;
    }

    public boolean isGuestReady() {
        return guestReady;
    }

    public void setGuestReady(boolean guestReady) {
        this.guestReady = guestReady;
    }

    public void setGuest(WebSocketSession guest) {
        this.guest = guest;
    }

    public void setGuestName(String guestName) {
        this.guestName = guestName;
    }

    public List<Integer> getMoves(){
        return new ArrayList<>(this.gameLogic.getMoves());
    }

    public int getWinFlag(){
        return this.gameLogic.getWinFlag();
    }

    public Match summaryMatch(){
        if(this.masterStone==1 && this.guestStone==2)
            return new Match(this.masterName,
                    this.guestName,
                    this.gameLogic.getMoves(),
                    this.gameLogic.getWinFlag());
        else if(masterStone==2 && guestStone==1)
            return new Match(this.guestName,
                    this.masterName,
                    this.gameLogic.getMoves(),
                    this.gameLogic.getWinFlag());
        else
            return null;
    }

    public void setStones(boolean masterBlack){
        if (masterBlack){
            this.masterStone = 1;
            this.guestStone = 2;
        } else {
            this.masterStone = 2;
            this.guestStone = 1;
        }
    }

    public int getMasterStone(){
        return this.masterStone;
    }
}
