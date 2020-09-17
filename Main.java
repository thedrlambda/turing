import java.io.*;
import java.nio.file.*;
public class Main {

  private static class Pair<A, B> {
    public final A left;
    public final B right;
    public Pair(A left, B right){
      this.left = left;
      this.right = right;
    }
  }

  private interface TapeVisitor<T> {
    T case_(EndOfTape t);
    T case_(Cons t);
  }
  private interface Tape {
    <T> T match(TapeVisitor<T> visitor);
  }
  private static class EndOfTape implements Tape {
    public <T> T match(TapeVisitor<T> visitor){
      return visitor.case_(this);
    }
  }
  private static class Cons implements Tape {
    public final char symbol;
    public final Tape rest;
    public Cons(char symbol, Tape rest){
      this.symbol = symbol;
      this.rest = rest;
    }
    public <T> T match(TapeVisitor<T> visitor){
      return visitor.case_(this);
    }
  }

  private static class WholeTape {
    public final Tape behind;
    public final Tape ahead;
    public WholeTape(Tape behind, Tape ahead) {
      this.behind = behind;
      this.ahead = ahead;
    }
  }

  private static char read(WholeTape t) {
    return t.ahead.match(new TapeVisitor<Character>() {
      public Character case_(EndOfTape t) {
        return '_';
      }
      public Character case_(Cons t) {
        return t.symbol;
      }
    });
  }

  private static WholeTape write(WholeTape t, char symbol) {
    return new WholeTape(
      t.behind, 
      t.ahead.match(new TapeVisitor<Tape>() {
        public Tape case_(EndOfTape t) {
          return new Cons(symbol, t);
        }
        public Tape case_(Cons t) {
          return new Cons(symbol, t.rest);
        }
    }));
  }

  private static WholeTape moveRight(WholeTape t){
    Cons head = t.ahead.match(new TapeVisitor<Cons>(){
      public Cons case_(EndOfTape t) {
        return new Cons('_', t);
      }
      public Cons case_(Cons t) {
        return t;
      }
    });
    return new WholeTape(
      new Cons(head.symbol, t.behind), 
      head.rest);
  }

  private static WholeTape moveLeft(WholeTape t){
    Cons neck = t.behind.match(new TapeVisitor<Cons>(){
      public Cons case_(EndOfTape t) {
        return new Cons('_', t);
      }
      public Cons case_(Cons t) {
        return t;
      }
    });
    return new WholeTape(
      neck.rest,
      new Cons(neck.symbol, t.ahead));
  }  

  interface DirectionAction {
    WholeTape execute(WholeTape t);
  }
  enum Dir {
    Left(Main::moveLeft), Right(Main::moveRight), Stay(t -> t);
    private DirectionAction d;
    Dir(DirectionAction d) {
      this.d = d;
    }
    public WholeTape move(WholeTape t) {
      return d.execute(t);
    }
  }

  private static class Action {
    public final String state;
    public final char symbol;
    public final Dir direction;
    public Action(String state, char symbol, Dir direction){
      this.state = state;
      this.symbol = symbol;
      this.direction = direction;
    }
  }

  private static class Instruction {
    public final String state;
    public final char symbol;
    public final Action action;
    public Instruction(String state, char symbol, Action action){
      this.state = state;
      this.symbol = symbol;
      this.action = action;
    }
  }

  private static Action action(Instruction[] program, String state, char symbol) {
    for (int i = 0 ; i < program.length; i++) {
      if(program[i].state.equals(state) 
      && (program[i].symbol == symbol 
        || program[i].symbol == '*' 
        && symbol != '_')) {
        return program[i].action;
      }
    }
    throw new RuntimeException("You forgot a state, dummy: " + state + ", " + symbol);
  }

  private static Instruction[] fromFile(String fileName) throws IOException {
    return Files.lines(Paths.get(fileName)).map(x -> {
      var buffer = x.split("\\(");
      var read = buffer[0].split(" ");
      var act = buffer[1].split(",");
      return new Instruction(
        read[0], 
        read[1].charAt(0), 
        new Action(
          act[0],
          act[1].trim().charAt(0),
          act[2].trim().charAt(0) == 'R' ? 
          Dir.Right : 
          act[2].trim().charAt(0) == 'S' ? 
          Dir.Stay : 
          Dir.Left
        ));
    }).toArray(Instruction[]::new);
  }

  private static Pair<WholeTape, String> step(Instruction[] program, WholeTape tape, String state){
    var symbol = read(tape);
    var action = action(program, state, symbol);
    var newTape = write(tape, action.symbol == '*' ? symbol : action.symbol);
    return new Pair<>(
      action.direction.move(newTape), 
      action.state);
  }

  private static void printTape(WholeTape t) {
    String before = t.behind.match(new TapeVisitor<String>() {
      public String case_(EndOfTape t) {
        return "";
      }
      public String case_(Cons t) {
        return t.rest.match(this) + t.symbol;
      }
    });
    String line2 = "";
    for(int i = 0; i < before.length(); i++) {
      line2 += " ";
    }
    String after = t.ahead.match(new TapeVisitor<String>(){
      public String case_(EndOfTape t) {
        return "";
      }
      public String case_(Cons t) {
        return t.symbol + t.rest.match(this);
      }
    });
    System.out.println(before + after);
    System.out.println(line2 + "^");
  } 

  private static WholeTape tapeFromString(String tape) {
    Tape ahead = new EndOfTape();
    for (int i = tape.length() - 1; i >= 0; i--) {
      ahead = new Cons(tape.charAt(i), ahead);
    }
    return new WholeTape(new EndOfTape(), ahead);
  }

  private static WholeTape execute(Instruction[] program, WholeTape t) {
    Pair<WholeTape, String> output = new Pair<>(t, "q0");
    while (!output.right.equals("H")) {
      output = step(program, output.left, output.right);
      printTape(output.left);
      try {
        Thread.sleep(300);
      } catch (Exception e) {}
    }
    return output.left;
  }

  public static void main(String[] args) throws IOException {
    WholeTape t = tapeFromString("010_110_010");
    var top = fromFile("top.tm");
    var add = fromFile("add.tm");
    var pop = fromFile("pop.tm");
    var inc = fromFile("inc.tm");
    var dup = fromFile("dup.tm");
    printTape(t);
    t = execute(top, t);
    // t = execute(add, t);
    // t = execute(add, t);
    // t = execute(pop, t);
    // t = execute(inc, t);
    t = execute(dup, t);
  }

}