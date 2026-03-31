package uk.ac.bris.cs.scotlandyard.model;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.*;

import com.google.common.graph.ImmutableValueGraph;
import com.google.common.io.Resources;
import jakarta.annotation.Nonnull;

import org.checkerframework.checker.nullness.qual.NonNull;
import uk.ac.bris.cs.scotlandyard.model.Board.GameState;
import uk.ac.bris.cs.scotlandyard.model.Move.*;
import uk.ac.bris.cs.scotlandyard.model.Piece.*;
import uk.ac.bris.cs.scotlandyard.model.ScotlandYard.*;
import uk.ac.bris.cs.scotlandyard.model.ScotlandYard.Factory;
import uk.ac.bris.cs.scotlandyard.model.ScotlandYard.Transport;

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
		private ImmutableSet<Piece> winners;

		@Nonnull
		@Override public GameSetup getSetup() {
			return this.setup;
		}

		@Override  public ImmutableSet<Piece> getPlayers() {
			System.out.println("creating ...");
			Set<Piece> players = new HashSet<>();
			players.add(mrX.piece());
			for (Player d : detectives) {
				players.add(d.piece());
			}
			return ImmutableSet.copyOf(players);
		}
		@Override public GameState advance(Move move) {  return null;  }
		@Override public Optional<Integer> getDetectiveLocation(Detective detective) {
			Player detectiveAsPlayer = pieceToPlayer(detective);
			if (detectiveAsPlayer != null) {
				return Optional.of(detectiveAsPlayer.location());
			} else {
				return Optional.empty();
			}
		}

		@Override public Optional<TicketBoard> getPlayerTickets(Piece piece) {
			Player p = pieceToPlayer(piece);
			ImmutableMap<Ticket, Integer> tickets = pieceToPlayer(piece).tickets();
			Optional<TicketBoard> ticketBoard = Optional.of(new TicketBoard() {
				@Override public int getCount(Ticket ticket) {
					switch(ticket) {
						case TAXI:
							return tickets.get(Ticket.TAXI);
						case BUS:
							return tickets.get(Ticket.BUS);
						case UNDERGROUND:
							return tickets.get(Ticket.UNDERGROUND);
						case DOUBLE:
							return tickets.get(Ticket.DOUBLE);
						case SECRET:
							return tickets.get(Ticket.SECRET);
						default:
							throw new IllegalArgumentException("Ticket type invalid.");
					}
				}
			});
			return ticketBoard;
		}

		@Nonnull
		@Override public ImmutableList<LogEntry> getMrXTravelLog(){
			return log;
		}
		@Nonnull
		@Override public ImmutableSet<Piece> getWinner(){
			return null;
		}
		@Override public ImmutableSet<Move> getAvailableMoves(){return null;}

		private Player pieceToPlayer(Piece piece) {
			if (piece.webColour() == "#000") { return mrX; }
			else {
				for (Player p : detectives) {
					if (piece == p.piece()) {
						return p;
					}
				}
			}
			return null;
		}

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
			//this.winners = new HashSet<>();
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
			// check no detectives are mrX
			// auto checks more than 1 mrX, as if theres one in detectives list the program throws anyway
			for (Player d : detectives) {
				if (d.isDetective()) {
					if (d == null) throw new NullPointerException("Detectives list contains a null.");
					if (d.has(Ticket.DOUBLE)) throw new IllegalArgumentException("Detective has DOUBLE ticket");
					else if (d.piece().webColour() == "#000") throw new IllegalArgumentException("Detective cannot be a black piece");
				}
				else throw new IllegalArgumentException("Mr X is in the detectives list.");
			}
			// check for mrX
			if (mrX.piece().webColour() != "#000") throw new IllegalArgumentException("There is no Mr X piece.");


			// check no detective locations overlap
			/*
			for (int i = 0; i < size(detectives); i++) {
				for (int j = i-1; j < size(detectives); j++) {
					if (detectives.get(i).location() != detectives.get(j).location()) {
						throw new IllegalArgumentException("2 Detectives are in the same location.");
					}
				}
			}*/

			// check winner is initially empty




			/* TODO: TESTS
			* empty moves throw 								DONE
			* get graph matches supplied						...
			* null detectives should throw						DONE
			* detective has double should throw					DONE
			* winning plauers empty initially					...
			* empty graph throws								DONE, Because it won't match standardgraph()
			* no mr x throws									DONE
			* swapped mr x throw								DONE
			* two plauer works									PERCHANCE
			* location overlap between detectives throws		DONE
			* get player tickets match supplied
			* get players match supplied						DONE
			* getplayertickets for non existent player is empty
			* null mr x throws									DONE
			* get move matches supplied
			* get player location for nonexistent player empty	DONE
			* six player works									PERCHANCE
			* detectives have secret ticket throws
			* get detective location matches supplied			DONE
			* null detectives throw								DONE
			* more than 1 mr x throws							DONE
			* */


		}
	}

}
