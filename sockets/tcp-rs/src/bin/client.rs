use std::{
    io::{Read, Write},
    net::TcpStream,
};

fn input<T: std::str::FromStr>(msg: &str) -> T {
    print!("{msg}");
    std::io::stdout().flush().expect("error");

    let mut input_str = String::new();
    std::io::stdin().read_line(&mut input_str).unwrap();
    input_str.trim().parse().ok().unwrap()
}

fn main() -> std::io::Result<()> {
    let mut stream = TcpStream::connect("127.0.0.1:7878")?;
    println!("Connected to a server!");

    loop {
        let input: String = input("> ");
        if input == "/quit" {
            break;
        }
        stream.write_all(input.as_bytes())?;

        let mut buffer = [0; 512];
        let response = stream.read(&mut buffer)?;
        println!("Server: {}", String::from_utf8_lossy(&buffer[..response]));
    }

    Ok(())
}
