package uk.ac.bris.cs.scotlandyard.model;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;

import java.io.IOException;
import java.util.*;

import com.google.common.graph.ImmutableValueGraph;
import jakarta.annotation.Nonnull;

import uk.ac.bris.cs.scotlandyard.model.Board.GameState;
import uk.ac.bris.cs.scotlandyard.model.Move.*;
import uk.ac.bris.cs.scotlandyard.model.Piece.*;
import uk.ac.bris.cs.scotlandyard.model.ScotlandYard.*;
import uk.ac.bris.cs.scotlandyard.model.ScotlandYard.Factory;

// imported graph to check against default graph
import static io.atlassian.fugue.Iterables.size;
import static uk.ac.bris.cs.scotlandyard.model.ScotlandYard.readGraph;
import static uk.ac.bris.cs.scotlandyard.model.ScotlandYard.standardGraph;

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

		@Nonnull
		@Override public GameSetup getSetup() {
			return this.setup;
		}

		@Override  public ImmutableSet<Piece> getPlayers() { return null; }
		@Override public GameState advance(Move move) {  return null;  }
		@Override public Optional<Integer> getDetectiveLocation(Detective detective) {return null;}
		@Override public Optional<TicketBoard> getPlayerTickets(Piece piece) {return null;}

		@Nonnull
		@Override public ImmutableList<LogEntry> getMrXTravelLog(){
			return log;
		}
		@Nonnull
		@Override public ImmutableSet<Piece> getWinner(){
			return this.winner;
		}
		@Override public ImmutableSet<Move> getAvailableMoves(){return null;}

		// constructor
		private MyGameState(
				final GameSetup setup,
				final ImmutableSet<Piece> remaining,
				final ImmutableList<LogEntry> log,
				final Player mrX,
				final List<Player> detectives) {

			this.setup = setup;
			this.remaining = remaining;
			this.log = log;
			this.mrX = mrX;
			this.detectives = detectives;

			// check that parameters handed over aren't null
			if (mrX == null || detectives == null || setup == null) {
				throw new NullPointerException("Build has been passed null arguments.");
			}
			// checks if any list structures handed over are empty
			else if (remaining.isEmpty() || log.isEmpty() || detectives.isEmpty()){
				throw new IllegalArgumentException("Build has been passed empty arguments.");
			}
			// check empty moves throw
			if(setup.moves.isEmpty()) throw new IllegalArgumentException("Moves is empty!");





			// initializing variables
			/*
			ImmutableValueGraph<Integer, ImmutableSet<Transport>> standardGraph;
			try {
				standardGraph = standardGraph();
			} catch (IOException e) {
				System.out.println("Standard graph not found. IO error.");
			}
			this.setup = new GameSetup(standardGraph, ImmutableList.of(true, false, true, true));
			// this.winner = new ImmutableSet<>();*/




			// TODO
			// failing tests (in GameStateCreationTest)
			// testGetGraphMatch
			// testWinningPlayerIsEmptyInitially
			// testTwoPlayerWorks
			// testGetPlayerTicketsMatchesSupplied
			//



		}
	}

}
