<?php
require_once "db.php";

$data = json_decode(file_get_contents("php://input"), true);

if (!isset($data['usuario']) || !isset($data['password'])) {
    echo json_encode(["success" => false, "mensaje" => "Por favor proporcione usuario y contraseña."], JSON_UNESCAPED_UNICODE);
    exit();
}

$user = trim($data['usuario']);
$pass = trim($data['password']);

$query = "SELECT u.id, u.nombre_completo, u.usuario, u.password, r.nombre AS rol
          FROM usuarios u
          INNER JOIN roles r ON u.rol_id = r.id
          WHERE u.usuario = :usuario LIMIT 1";

$stmt = $conn->prepare($query);
$stmt->bindParam(":usuario", $user);
$stmt->execute();

if ($stmt->rowCount() > 0) {
    $row = $stmt->fetch(PDO::FETCH_ASSOC);
    if ($pass === $row['password'] || password_verify($pass, $row['password'])) {
        echo json_encode([
            "success" => true,
            "mensaje" => "Inicio de sesión exitoso.",
            "usuario" => [
                "id" => (string)$row['id'],
                "nombre_completo" => $row['nombre_completo'],
                "usuario" => $row['usuario'],
                "rol" => $row['rol']
            ]
        ], JSON_UNESCAPED_UNICODE);
    } else {
        echo json_encode(["success" => false, "mensaje" => "Contraseña incorrecta."], JSON_UNESCAPED_UNICODE);
    }
} else {
    echo json_encode(["success" => false, "mensaje" => "Usuario no encontrado."], JSON_UNESCAPED_UNICODE);
}
?>
