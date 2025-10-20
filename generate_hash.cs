using System;

public class Program
{
    public static void Main()
    {
        var password = "password";
        var hash1 = BCrypt.Net.BCrypt.HashPassword(password);
        var hash2 = BCrypt.Net.BCrypt.HashPassword(password);
        
        Console.WriteLine($"Hash for admin123: {hash1}");
        Console.WriteLine($"Hash for user123: {hash2}");
        Console.WriteLine($"Verification test: {BCrypt.Net.BCrypt.Verify(password, hash1)}");
    }
}