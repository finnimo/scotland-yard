package uk.ac.bris.cs.scotlandyard.model;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.*;

import com.google.common.graph.ImmutableValueGraph;
import com.google.common.io.Resources;
import jakarta.annotation.Nonnull;

import uk.ac.bris.cs.scotlandyard.model.Board.GameState;
import uk.ac.bris.cs.scotlandyard.model.Move.*;
import uk.ac.bris.cs.scotlandyard.model.Piece.*;
import uk.ac.bris.cs.scotlandyard.model.ScotlandYard.*;
import uk.ac.bris.cs.scotlandyard.model.ScotlandYard.Factory;

// imported graph to check against default graph
import static io.atlassian.fugue.Iterables.size;
import static uk.ac.bris.cs.scotlandyard.model.ScotlandYard.*;

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
			System.out.println("Before null tests");

			// check that parameters handed over aren't null

			if (mrX == null || detectives == null || setup == null) {
				throw new NullPointerException("Build has been passed null arguments.");
			}
			// checks if any list structures handed over are empty
			else if (detectives.isEmpty()){
				throw new IllegalArgumentException("Build has been passed empty arguments.");
			}
			// check empty moves throw
			if(setup.moves.isEmpty()) throw new IllegalArgumentException("Moves is empty!");


			System.out.println("Null and empty checks passed");

			// checking if graph and moves = standard graph and moves

			GameSetup standardSetup;
			ImmutableValueGraph<Integer, ImmutableSet<Transport>> standardGraph;
			try {
				standardGraph = standardGraph();
				standardSetup = new GameSetup(standardGraph, STANDARD24MOVES);
				//this.setup = new GameSetup(standardGraph, ImmutableList.of(true, false, true, true));
				if (!this.setup.equals(standardSetup)) {
					throw new IllegalArgumentException("Setup does not contain either standard moves or standard graph.");
				} else {
					System.out.println("Given graphs and moves are standard.");
				}

			} catch (IOException e) {
				System.out.println("Standard graph not found. IO error.");
			}

			// check detectives dont have double tickets
			// check mrX exists
			// check mrX is black, check no players are black
			for (Player d : detectives) {
				if (d.isDetective() && d.has(Ticket.DOUBLE)) throw new IllegalArgumentException("Detective has DOUBLE ticket");
			}
			boolean existsMrX = false;
			for (Player d : detectives) {
				if (d.isDetective()) {
					if (d.has(Ticket.DOUBLE)) throw new IllegalArgumentException("Detective has DOUBLE ticket");
					else if (d.piece().webColour() == "#000") throw new IllegalArgumentException("Detective cannot be a black piece");
				}

				else if (d.piece().webColour() != "#000") throw new IllegalArgumentException("Mr X must be a black piece");
			}
			if (!existsMrX) throw new IllegalArgumentException("There is no mrX.");




			/* TODO: TESTS
			* empty moves throw 								DONE
			* get graph matches supplied						...
			* null detectives should throw						DONE
			* detective has double should throw					DONE
			* winning plauers empty initially
			* empty graph throws								DONE, Because it won't match standardgraph()
			* no mr x throws									DONE
			* swapped mr x throw								DONE
			* two plauer works
			* location overlap between detectives throws
			* get player tickets match supplied
			* get players match supplied
			* getplayer tickets for non existent player is empty
			* null mr x throws
			* get move matches supplied
			* get player location for nonexistent player empty	IN PROGRESS ...
			* six player works
			* detectives have secretticket throws
			* get detective location matches supplied
			* null detectives throw
			* more than 1 mr x throws
			* */


		}
	}

}
