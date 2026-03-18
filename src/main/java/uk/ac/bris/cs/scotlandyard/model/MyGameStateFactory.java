package uk.ac.bris.cs.scotlandyard.model;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import java.util.*;
import jakarta.annotation.Nonnull;

import uk.ac.bris.cs.scotlandyard.model.Board.GameState;
import uk.ac.bris.cs.scotlandyard.model.Move.*;
import uk.ac.bris.cs.scotlandyard.model.Piece.*;
import uk.ac.bris.cs.scotlandyard.model.ScotlandYard.*;
import uk.ac.bris.cs.scotlandyard.model.ScotlandYard.Factory;

/**
 * cw-model
 * Stage 1: Complete this class
 */
public final class MyGameStateFactory implements Factory<GameState> {

	@Nonnull @Override public GameState build(
			GameSetup setup,
			Player mrX,
			ImmutableList<Player> detectives) {
		// TODO
		return new MyGameState(setup, ImmutableSet.of(MrX.MRX), ImmutableList.of(), mrX, detectives);

	}
	private final class MyGameState implements GameState {
		private GameSetup setup;
		private ImmutableSet<Piece> remaining;
		private ImmutableList<LogEntry> log;
		private Player mrX;
		private List<Player> detectives;
		private ImmutableSet<Move> moves;
		private ImmutableSet<Piece> winner;

		@Override public GameSetup getSetup() {
			return setup;
		}
		@Override  public ImmutableSet<Piece> getPlayers() { return null; }
		@Override public GameState advance(Move move) {  return null;  }
		@Override public Optional<Integer> getDetectiveLocation(Detective detective) {return null;}
		@Override public Optional<TicketBoard> getPlayerTickets(Piece piece) {return null;}
		@Override public ImmutableList<LogEntry> getMrXTravelLog(){
			return log;
		}
		@Override public ImmutableSet<Piece> getWinner(){return null;}
		@Override public ImmutableSet<Move> getAvailableMoves(){return null;}

		private MyGameState(
				final GameSetup setup,
				final ImmutableSet<Piece> remaining,
				final ImmutableList<LogEntry> log,
				final Player mrX,
				final List<Player> detectives){
			this.setup = setup;
			this.remaining = remaining;
			this.log = log;
			this.mrX = mrX;
			this.detectives = detectives;

			// TODO
			// Need to implement the following checks/tests
			// Check that all detectives have different locations
			// MrX is indeed the black piece
			// There are no duplicate game pieces
			// Check theres only one mrX
			// testSwappedMrXShouldThrow <- idk what this means
			// check that mrX has double tickets
			// Check that detectives have secret tickets
			// Check for empty moves
			// Check for empty graph
			//

			if (setup == null || remaining.isEmpty() || log.isEmpty() || mrX == null || detectives.isEmpty()){
				throw new IllegalArgumentException("Arguments cant be null.");

			}




		}
	}

}
