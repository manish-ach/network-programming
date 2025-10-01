use std::{
    fs,
    io::{Read, Write},
    net::{TcpListener, TcpStream},
};

fn handle_client(mut stream: TcpStream) {
    let mut buffer = [0; 512];
    if let Ok(_) = stream.read(&mut buffer) {
        let request = String::from_utf8_lossy(&buffer);

        let path = request
            .lines()
            .next()
            .and_then(|line| line.split_whitespace().nth(1))
            .unwrap_or("/");

        let path = if path == "/" { "index.html" } else { path };
        let filepath = format!("public/{}", path);
        let body = fs::read_to_string(&filepath)
            .unwrap_or_else(|_| "<h1>404 - File not Found :P</h1>".to_string());

        let content_type = if filepath.ends_with(".html") {
            "text/html"
        } else if filepath.ends_with(".css") {
            "text/css"
        } else if filepath.ends_with(".js") {
            "application/javascript"
        } else {
            "text/plain"
        };

        let response = format!(
            "HTTP/1.1 200 OK\r\nContent-Type: {}; charset\r\n\r\n{}",
            content_type, body
        );

        stream.write_all(response.as_bytes()).unwrap();
    }
}

fn main() -> std::io::Result<()> {
    let listener = TcpListener::bind("127.0.0.1:8001")?;
    println!("Server listening on http://127.0.0.1:8001");

    for stream in listener.incoming() {
        if let Ok(stream) = stream {
            std::thread::spawn(|| handle_client(stream));
        }
    }

    Ok(())
}
