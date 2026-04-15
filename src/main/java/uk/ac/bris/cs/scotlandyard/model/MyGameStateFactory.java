package uk.ac.bris.cs.scotlandyard.model;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;

import java.util.*;

import jakarta.annotation.Nonnull;

import uk.ac.bris.cs.scotlandyard.model.Board.GameState;
import uk.ac.bris.cs.scotlandyard.model.Move.*;
import uk.ac.bris.cs.scotlandyard.model.Piece.*;
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
		return new MyGameState(setup, ImmutableSet.of(MrX.MRX), ImmutableList.of(), mrX, detectives);
	}
	private final class MyGameState implements GameState {
		private final GameSetup setup;
		private final ImmutableSet<Piece> remaining;
		private final ImmutableList<LogEntry> log;
		private final Player mrX;
		private final List<Player> detectives;
		private ImmutableSet<Move> moves;
		private final ArrayList<Piece> winners;

		@Nonnull
		@Override public GameSetup getSetup() {
			return this.setup;
		}

		@Override  public ImmutableSet<Piece> getPlayers() {
			Set<Piece> players = new HashSet<>();
			players.add(mrX.piece());
			for (Player d : detectives) {
				players.add(d.piece());
			}
			return ImmutableSet.copyOf(players);
		}

		@Override public GameState advance(Move move) {
			Player player = pieceToPlayer(move.commencedBy());
			Player newPlayer;
			ArrayList<Player> newDetectives = new ArrayList<>(detectives);
			List<LogEntry> newLog = new ArrayList<>(log);

			if(!moves.contains(move)) throw new IllegalArgumentException("Illegal move: "+move);

			Ticket firstTicket = move.tickets().iterator().next();
			ImmutableList<Integer> finalDestList = move.accept(new MyGameState.destinationVisitor());
			if (player.isMrX()) {
				int moveCount = 0;
				Ticket secondTicket = null;
				for (Ticket t : move.tickets()) {
					moveCount++;
					if (moveCount == 2) { secondTicket = t; }
				} // count tells us it's double
				player = player.use(move.tickets());
				if (moveCount < 2) {
					if (setup.moves.get(size(log))) { newLog.add(LogEntry.reveal(firstTicket, finalDestList.get(0))); }
					else { newLog.add(LogEntry.hidden(firstTicket)); }
					newPlayer = new Player(player.piece(), player.tickets(), finalDestList.get(0));
				} else {
					logDouble(firstTicket, secondTicket, finalDestList, newLog);
					newPlayer = new Player(player.piece(), player.tickets(), finalDestList.get(1));
				}

				return new MyGameState(setup, updateRemaining(player), ImmutableList.copyOf(newLog), newPlayer, detectives);

			} else {
				newDetectives.remove(player);
				player = player.use(firstTicket);
				newPlayer = new Player(player.piece(), player.tickets(), finalDestList.get(0));
				newDetectives.add(newPlayer);
				return new MyGameState(setup, updateRemaining(player), log, mrX.give(move.tickets()), newDetectives);
			}
		}

		List<LogEntry> logDouble(Ticket firstTicket, Ticket secondTicket, ImmutableList<Integer> finalDestList, List<LogEntry> newLog) {
			// log first ticket
			if (setup.moves.get(size(log))) {
				newLog.add(LogEntry.reveal(firstTicket, finalDestList.get(0)));
			} else {
				newLog.add(LogEntry.hidden(firstTicket));
			}
			// log second ticket
			if (setup.moves.get(size(log)+1)) {
				newLog.add(LogEntry.reveal(secondTicket, finalDestList.get(1)));
			} else {
				newLog.add(LogEntry.hidden(secondTicket));
			}
			return newLog;
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
					return tickets.get(ticket);
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
			if (winners ==  null) { return ImmutableSet.of(); }
			return ImmutableSet.copyOf(this.winners);
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
		private ImmutableSet<Piece> updateRemaining(Player player) {
			HashSet<Piece> newRemaining = new HashSet<>(remaining);
			newRemaining.remove(player.piece());

			if (log.size() == setup.moves.size() && player.isDetective() && remaining.size() == 1) { return ImmutableSet.of(); }
			if (player.isMrX()) {
				newRemaining.addAll(getDetectivePieces());
			} else {
				HashSet<Piece> detectivesToRemove = new HashSet<>();
				// remove empty detectives
				for (Piece piece : newRemaining) {
					Player detective = pieceToPlayer(piece);
					Set <SingleMove> detectiveMoves = makeSingleMoves(detective, detective.location());
					if (detectiveMoves.isEmpty()) {
						detectivesToRemove.add(detective.piece());
					}
				}
				newRemaining.removeAll(detectivesToRemove);
				if (newRemaining.isEmpty()) {
					newRemaining.add(mrX.piece());
				}
			}
			return ImmutableSet.copyOf(newRemaining);
		}

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
			System.out.println(remaining + "remaining");
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
			// check no detective is null
			// check detectives dont have double or secret tickets
			// check no detectives are mrX
			for (Player d : detectives) {
				if (d.isDetective()) {
					if (d == null) throw new NullPointerException("Detectives list contains a null.");
					if (d.has(Ticket.DOUBLE) || d.has(Ticket.SECRET)) throw new IllegalArgumentException("Detective has DOUBLE ticket");
					else if (d.piece().webColour() == "#000") throw new IllegalArgumentException("Detective cannot be a black piece");
				}
				else throw new IllegalArgumentException("Mr X is in the detectives list.");
			}
			// check for mrX existing based on piece colours
			if (mrX.piece().webColour() != "#000") throw new IllegalArgumentException("There is no Mr X piece.");

			// check no detective locations overlap TODO: STUDY THIS
			for (int i = 0; i < size(detectives); i++) {
				for (int j = i+1; j < size(detectives); j++) {
					if (detectives.get(i).location() == detectives.get(j).location()) {
						throw new IllegalArgumentException("2 Detectives are in the same location.");
					}
				}
			}

			// check graph not empty
			if (setup.graph.nodes().size() == 0) {
				throw new IllegalArgumentException("Graph is empty.");
			}

			// calculate remaining moves and winners
			this.moves = ImmutableSet.copyOf(remainingMoves());
			this.winners = new ArrayList<>(calculateWinner());

			if (!this.winners.isEmpty()) {
				this.moves = ImmutableSet.of();
			}
		}

		private ImmutableSet<Piece> getDetectivePieces(){
			Set<Piece> pieces = new HashSet<>();
			for (Player detective: detectives){
				pieces.add(detective.piece());
			}
			return ImmutableSet.copyOf(pieces);
		}
		private Set<Move> remainingMoves() {
			Set<Move> allMoves = new HashSet<>();
			for (Piece piece : remaining) {
				Player player = pieceToPlayer(piece);
				Set<SingleMove> pieceMovesAllowed = makeSingleMoves(player, player.location());
				allMoves.addAll(pieceMovesAllowed);

				if (player.isMrX()) {
					allMoves.addAll(makeDoubleMoves(player, player.location()));
				}
			}
			return allMoves;
		}
		private Set<SingleMove> makeSingleMoves(Player player, int source){
			HashSet<SingleMove> allMoves = new HashSet<>();

			/* for every node adjacent to current player,
			 * 	check if node occupied. if so, move onto next node.
			 * now, for nodes that can be traveled to:
			 * 	find transport from source to dest node
			 * 		if player has, make a move, else do nothing
			 * lastly, for every adjacent node, mrX will have access to that if they have ticket
			 * */

			for(int destination : setup.graph.adjacentNodes(source)) {
				boolean nodeOccupied = false;
				for (Player detective : detectives){
					if (detective.location() == destination){ nodeOccupied = true; }
				}
				if (nodeOccupied) { continue; }
				for(Transport t : setup.graph.edgeValueOrDefault(source, destination, ImmutableSet.of()) ) {
					Ticket ticket = t.requiredTicket();
					if (player.has(ticket)){
						allMoves.add(new SingleMove(player.piece(), source, ticket, destination));
					}
				}
				// add secret move
				if (player.isMrX() && player.has(Ticket.SECRET)){
					allMoves.add(new SingleMove(player.piece(), source, Ticket.SECRET, destination));
				}

			}
			return allMoves;
		}
		private Set<DoubleMove> makeDoubleMoves(Player player, int source){
			Set<DoubleMove> allDoubleMoves = new HashSet<>();
			// if player has no double, return
			// double takes up 2 slots, if log size is <2 from max
			if (!player.has(Ticket.DOUBLE)) return allDoubleMoves;
			if (setup.moves.size() - log.size() < 2) return allDoubleMoves;

			// first need to get all available single moves. we're gonna be doing two single moves in a row and we already have the logic for it
			Set<SingleMove> firstMoves = makeSingleMoves(player, source);

			for (SingleMove firstMove : firstMoves){
				// now we're gonna loop through every possibility here! to find our choices for the second move
				Set<SingleMove> secondMoves = makeSingleMoves(player, firstMove.destination);
				for (SingleMove second : secondMoves){
					// now we're gonna check if mrX actually has the tickets for this.
					// we iterate over the first move, check if we can do that move, then iterate over EVERY possible second move you can make from that first move and check the tickets then
					// if the two moves are different, eg taxi then bus we dont need to worry since we already checked hasTicket in makesinglemoves
					// our issue is if the two moves are the SAME we need to check if he has enough tickets
					boolean validTickets;

					if (firstMove.ticket == second.ticket){
						validTickets = player.hasAtLeast(firstMove.ticket, 2);
					}
					else{
						// different tickets
						validTickets = true;
					}

					if (validTickets){
						allDoubleMoves.add(new DoubleMove(player.piece(), source, firstMove.ticket, firstMove.destination, second.ticket, second.destination));
					}

				}

			}
			return allDoubleMoves;
		}
		// move visits single and double
		// Move.Visitor will visit these
		// to help advance
		final static class destinationVisitor implements Move.Visitor<ImmutableList<Integer>> {
			@Override
			public ImmutableList<Integer> visit(SingleMove move) {
				return ImmutableList.of(move.destination);
			}

			@Override
			public ImmutableList<Integer> visit(DoubleMove move) {
				return ImmutableList.of(move.destination1, move.destination2);
			}
		}

		private ImmutableSet<Piece> calculateWinner(){

			// check if detectives have one
			// 1. if detectives finish move on mrX
			// 2. there's no places for mrX to travel to
			for (Player detective: detectives){
				if (detective.location() == mrX.location()){
					return getDetectivePieces();
				}
			}

			if (remaining.contains(mrX.piece()) && moves.isEmpty()){
				return getDetectivePieces();
			}

			// detectives run out of tickets
			// mr x fills a log and detectives fail to catch him with final moves
			if (remaining.contains(mrX.piece())) { // detectives have no tickets left after mrX has moved
				System.out.println("mr x turn, checking for tickets for detectives");
				boolean noDetectiveTicketsLeft = true;
				for (Player d : detectives) {
					if (d.tickets().get(Ticket.BUS) != 0) { noDetectiveTicketsLeft = false; }
					if (d.tickets().get(Ticket.TAXI) != 0) { noDetectiveTicketsLeft = false; }
					if (d.tickets().get(Ticket.UNDERGROUND) != 0) { noDetectiveTicketsLeft = false; }
				}
				if (noDetectiveTicketsLeft) { return ImmutableSet.of(mrX.piece()); }
			}

			// checks mrX has had last turn. and that there are no moves left.
			if (log.size() == setup.moves.size() && !remaining.contains(mrX.piece()) && moves.isEmpty()) {
				return ImmutableSet.of(mrX.piece());
			}

			return ImmutableSet.of();
		}



	}

}