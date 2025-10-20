using Microsoft.AspNetCore.SignalR;
using Microsoft.AspNetCore.Authorization;
using Microsoft.EntityFrameworkCore;
using AutostradaAuctions.Api.Data;
using AutostradaAuctions.Api.Models;
using System.Security.Claims;

namespace AutostradaAuctions.Api.Hubs
{
    // [Authorize] - Temporarily disabled for testing
    public class BiddingHub : Hub
    {
        private readonly AuctionDbContext _context;
        private readonly ILogger<BiddingHub> _logger;

        public BiddingHub(AuctionDbContext context, ILogger<BiddingHub> logger)
        {
            _context = context;
            _logger = logger;
        }

        public async Task JoinAuction(int auctionId)
        {
            try
            {
                var groupName = $"auction_{auctionId}";
                await Groups.AddToGroupAsync(Context.ConnectionId, groupName);
                _logger.LogInformation($"User {Context.UserIdentifier} joined auction {auctionId}");
                
                // Send current auction status to the joining user
                var auction = await _context.Auctions.FindAsync(auctionId);
                if (auction != null)
                {
                    await Clients.Caller.SendAsync("AuctionStatus", new
                    {
                        AuctionId = auctionId,
                        CurrentBid = auction.CurrentBid,
                        Status = auction.Status.ToString(),
                        EndTime = auction.EndTime
                    });
                }
            }
            catch (Exception ex)
            {
                _logger.LogError(ex, $"Error joining auction {auctionId}");
            }
        }

        public async Task LeaveAuction(int auctionId)
        {
            try
            {
                var groupName = $"auction_{auctionId}";
                await Groups.RemoveFromGroupAsync(Context.ConnectionId, groupName);
                _logger.LogInformation($"User {Context.UserIdentifier} left auction {auctionId}");
            }
            catch (Exception ex)
            {
                _logger.LogError(ex, $"Error leaving auction {auctionId}");
            }
        }

        public async Task PlaceBid(int auctionId, decimal amount, string bidderName)
        {
            try
            {
                var userId = Context.UserIdentifier;
                if (string.IsNullOrEmpty(userId))
                {
                    await Clients.Caller.SendAsync("BidError", "Authentication required");
                    return;
                }

                var auction = await _context.Auctions
                    .Include(a => a.Bids)
                    .FirstOrDefaultAsync(a => a.Id == auctionId);

                if (auction == null)
                {
                    await Clients.Caller.SendAsync("BidError", "Auction not found");
                    return;
                }

                // Validate auction status
                if (auction.Status != AuctionStatus.Active)
                {
                    await Clients.Caller.SendAsync("BidError", "Auction is not active");
                    return;
                }

                // Check if auction has ended
                if (DateTime.UtcNow > auction.EndTime)
                {
                    await Clients.Caller.SendAsync("BidError", "Auction has ended");
                    return;
                }

                // Validate bid amount
                var minimumBid = Math.Max(auction.CurrentBid + 1, auction.StartingPrice);
                if (amount < minimumBid)
                {
                    await Clients.Caller.SendAsync("BidError", $"Bid must be at least ${minimumBid:F2}");
                    return;
                }

                // Check if user is not the seller
                if (auction.SellerId == int.Parse(userId))
                {
                    await Clients.Caller.SendAsync("BidError", "Sellers cannot bid on their own auctions");
                    return;
                }

                // Create bid
                var bid = new Bid
                {
                    AuctionId = auctionId,
                    BidderId = int.Parse(userId),
                    Amount = amount,
                    Timestamp = DateTime.UtcNow
                };

                _context.Bids.Add(bid);

                // Update auction current bid
                auction.CurrentBid = amount;

                await _context.SaveChangesAsync();

                // Load bidder for response
                await _context.Entry(bid)
                    .Reference(b => b.Bidder)
                    .LoadAsync();

                var bidDto = new BidDto
                {
                    Id = bid.Id,
                    Amount = bid.Amount,
                    Timestamp = bid.Timestamp,
                    BidderName = $"{bid.Bidder.FirstName} {bid.Bidder.LastName[0]}.",
                    AuctionId = auctionId,
                    IsWinning = bid.Amount == auction.CurrentBid
                };

                // Broadcast bid update to all users in the auction group
                var groupName = $"auction_{auctionId}";
                await Clients.Group(groupName).SendAsync("ReceiveBidUpdate", auctionId, bidDto);

                // Send success confirmation to the bidder
                await Clients.Caller.SendAsync("BidPlaced", bidDto);

                _logger.LogInformation($"Bid placed: User {userId}, Auction {auctionId}, Amount ${amount}");
            }
            catch (Exception ex)
            {
                _logger.LogError(ex, $"Error placing bid for auction {auctionId}");
                await Clients.Caller.SendAsync("BidError", "Failed to place bid. Please try again.");
            }
        }

        public override async Task OnConnectedAsync()
        {
            _logger.LogInformation($"User {Context.UserIdentifier} connected to bidding hub");
            await base.OnConnectedAsync();
        }

        public override async Task OnDisconnectedAsync(Exception? exception)
        {
            _logger.LogInformation($"User {Context.UserIdentifier} disconnected from bidding hub");
            await base.OnDisconnectedAsync(exception);
        }

        // Method to send auction status updates (called from auction monitoring service)
        public async Task SendAuctionStatusUpdate(int auctionId, string status)
        {
            var groupName = $"auction_{auctionId}";
            await Clients.Group(groupName).SendAsync("AuctionStatusChanged", auctionId, status);
        }

        // Method to send auction end notifications
        public async Task SendAuctionEndNotification(int auctionId, int? winnerId, decimal finalBid)
        {
            var groupName = $"auction_{auctionId}";
            await Clients.Group(groupName).SendAsync("AuctionEnded", new
            {
                AuctionId = auctionId,
                WinnerId = winnerId,
                FinalBid = finalBid
            });
        }
    }

    // DTO for SignalR bid updates
    public class BidDto
    {
        public int Id { get; set; }
        public decimal Amount { get; set; }
        public DateTime Timestamp { get; set; }
        public string BidderName { get; set; } = string.Empty;
        public int AuctionId { get; set; }
        public bool IsWinning { get; set; }
    }
}