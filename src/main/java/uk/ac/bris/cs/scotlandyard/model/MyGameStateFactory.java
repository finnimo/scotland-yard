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

		@Override public GameState advance(Move move) {
			if(!moves.contains(move)) throw new IllegalArgumentException("Illegal move: "+move);

			return null;
		}

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
			if (p == null) {return Optional.empty();}
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
		@Override public ImmutableSet<Move> getAvailableMoves(){
			return this.moves;
		}



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

			// check no detective is null
			// check detectives dont have double or secret tickets
			// check no detectives are mrX
			// auto checks more than 1 mrX, as if theres one in detectives list the program throws anyway
			for (Player d : detectives) {
				if (d.isDetective()) {
					if (d == null) throw new NullPointerException("Detectives list contains a null.");
					if (d.has(Ticket.DOUBLE) || d.has(Ticket.SECRET)) throw new IllegalArgumentException("Detective has DOUBLE ticket");
					else if (d.piece().webColour() == "#000") throw new IllegalArgumentException("Detective cannot be a black piece");
				}
				else throw new IllegalArgumentException("Mr X is in the detectives list.");
			}
			// check for mrX existing
			if (mrX.piece().webColour() != "#000") throw new IllegalArgumentException("There is no Mr X piece.");


			// check no detective locations overlap
			for (int i = 0; i < size(detectives); i++) {
				for (int j = i+1; j < size(detectives); j++) {
					if (detectives.get(i).location() == detectives.get(j).location()) {
						throw new IllegalArgumentException("2 Detectives are in the same location.");
					}
				}
			}

			// check empty graph
			if (setup.graph.nodes().size() == 0) {
				throw new IllegalArgumentException("Graph is empty.");
			}

			// check winner is initially empty


			// filling in the moves set
			Set<Move> allMoves = new HashSet<>();
			for (Piece piece : remaining) {
				Player player = pieceToPlayer(piece);
				Set<SingleMove> singleMove = makeSingleMoves(setup, detectives, player, player.location());
				allMoves.addAll(singleMove);

				if (player.isMrX()) {
					allMoves.addAll(makeDoubleMoves(setup, detectives, player, player.location(), log));
				}
			}

			//
			this.moves = ImmutableSet.copyOf(allMoves);




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
			 * get player tickets match supplied					DONE
			 * get players match supplied						DONE
			 * getplayertickets for non existent player is empty	...
			 * null mr x throws									DONE
			 * get move matches supplied							DONE
			 * get player location for nonexistent player empty	DONE
			 * six player works									PERCHANCE
			 * detectives have secret ticket throws
			 * get detective location matches supplied			DONE
			 * null detectives throw								DONE
			 * more than 1 mr x throws							DONE
			 * */


		}

		private static Set<SingleMove> makeSingleMoves(GameSetup setup, List<Player> detectives, Player player, int source){
			HashSet<SingleMove> allMoves = new HashSet<>();

			for(int destination : setup.graph.adjacentNodes(source)) {
				// TODO find out if destination is occupied by a detective
				//  if the location is occupied, don't add to the collection of moves to return

				boolean occupied = false;
				for (Player detective : detectives){
					if (detective.location() == destination){
						occupied = true;
						break;
					}
				}
				if (occupied){
					continue;
				}


				for(Transport t : setup.graph.edgeValueOrDefault(source, destination, ImmutableSet.of()) ) {
					Ticket ticket = t.requiredTicket();
					if (player.has(ticket)){
						allMoves.add(new SingleMove(player.piece(), source, ticket, destination));
					}
					// TODO find out if the player has the required tickets
					//  if it does, construct a SingleMove and add it the collection of moves to return
				}

				// TODO consider the rules of secret moves here
				//  add moves to the destination via a secret ticket if there are any left with the player

				if (player.isMrX() && player.has(Ticket.SECRET)){
					allMoves.add(new SingleMove(player.piece(), source, Ticket.SECRET, destination));
				}
			}

			// TODO return the collection of moves
			return allMoves;
		}

		private static Set<DoubleMove> makeDoubleMoves(GameSetup setup, List<Player> detectives, Player player, int source, ImmutableList<LogEntry> log){
			Set<DoubleMove> allDoubleMoves = new HashSet<>();

			if (!player.has(Ticket.DOUBLE)) return allDoubleMoves;
			if (setup.moves.size() - log.size() < 2) return allDoubleMoves;

			// first need to get all available single moves. we're gonna be doing two single moves in a row and we already have the logic for it
			Set<SingleMove> firstMoves = makeSingleMoves(setup, detectives, player, source);

			for (SingleMove first : firstMoves){
				// now we're gonna loop through every possibility here! to find our choices for the second move
				// we need a new hashset for this. itll be "our possible locations for the second move"
				// then we just check if its valid
				Set<SingleMove> secondMoves = makeSingleMoves(setup, detectives, player, first.destination);

				for (SingleMove second : secondMoves){
					// now we're gonna check if mrX actually has the tickets for this.
					// we iterate over the first move, check if we can do that move, then iterate over EVERY possible second move you can make from that first move and check the tickets then
					// if the two moves are different, eg taxi then bus we dont need to worry since we already checked hasTicket in makesinglemoves
					// our issue is if the two moves are the SAME we need to check if he has enough tickets

					boolean validTickets;

					if (first.ticket == second.ticket){
						validTickets = player.hasAtLeast(first.ticket, 2);
					}
					else{
						// different tickets
						validTickets = true;
					}

					if (validTickets){
						allDoubleMoves.add(new DoubleMove(player.piece(), source, first.ticket, first.destination, second.ticket, second.destination));
					}

				}

			}
			return allDoubleMoves;
		}
	}

}