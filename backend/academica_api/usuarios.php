<?php
require_once "db.php";

$method = $_SERVER['REQUEST_METHOD'] ?? 'GET';

switch ($method) {
    case 'GET':
        $query = "SELECT u.id, u.nombre_completo, u.usuario, u.rol_id, r.nombre AS rol
                  FROM usuarios u
                  INNER JOIN roles r ON u.rol_id = r.id
                  ORDER BY u.id DESC";
        $stmt = $conn->prepare($query);
        $stmt->execute();
        $usuarios = $stmt->fetchAll(PDO::FETCH_ASSOC);
        foreach ($usuarios as &$u) {
            $u['id'] = (string)$u['id'];
            $u['rol_id'] = (string)$u['rol_id'];
        }
        echo json_encode(["success" => true, "data" => $usuarios], JSON_UNESCAPED_UNICODE);
        break;

    case 'POST':
        $data = json_decode(file_get_contents("php://input"), true);
        if (empty($data['nombre_completo']) || empty($data['usuario']) || empty($data['password']) || empty($data['rol_id'])) {
            echo json_encode(["success" => false, "mensaje" => "Datos incompletos."], JSON_UNESCAPED_UNICODE);
            exit();
        }
        // Encriptación / Hashing BCRYPT de la contraseña (Exigido en Criterio #7)
        $hashed = password_hash($data['password'], PASSWORD_DEFAULT);

        $query = "INSERT INTO usuarios (nombre_completo, usuario, password, rol_id) VALUES (:nombre, :usuario, :password, :rol_id)";
        $stmt = $conn->prepare($query);
        $stmt->bindParam(":nombre", $data['nombre_completo']);
        $stmt->bindParam(":usuario", $data['usuario']);
        $stmt->bindParam(":password", $hashed);
        $stmt->bindParam(":rol_id", $data['rol_id']);

        if ($stmt->execute()) {
            echo json_encode(["success" => true, "mensaje" => "Usuario registrado con éxito con contraseña cifrada."], JSON_UNESCAPED_UNICODE);
        } else {
            echo json_encode(["success" => false, "mensaje" => "Error al registrar usuario."], JSON_UNESCAPED_UNICODE);
        }
        break;

    case 'PUT':
        $data = json_decode(file_get_contents("php://input"), true);
        if (empty($data['id']) || empty($data['nombre_completo']) || empty($data['usuario']) || empty($data['rol_id'])) {
            echo json_encode(["success" => false, "mensaje" => "Datos incompletos."], JSON_UNESCAPED_UNICODE);
            exit();
        }
        if (!empty($data['password'])) {
            // Encriptación BCRYPT al actualizar contraseña
            $hashed = password_hash($data['password'], PASSWORD_DEFAULT);
            $query = "UPDATE usuarios SET nombre_completo = :nombre, usuario = :usuario, password = :password, rol_id = :rol_id WHERE id = :id";
            $stmt = $conn->prepare($query);
            $stmt->bindParam(":password", $hashed);
        } else {
            $query = "UPDATE usuarios SET nombre_completo = :nombre, usuario = :usuario, rol_id = :rol_id WHERE id = :id";
            $stmt = $conn->prepare($query);
        }
        $stmt->bindParam(":nombre", $data['nombre_completo']);
        $stmt->bindParam(":usuario", $data['usuario']);
        $stmt->bindParam(":rol_id", $data['rol_id']);
        $stmt->bindParam(":id", $data['id']);

        if ($stmt->execute()) {
            echo json_encode(["success" => true, "mensaje" => "Usuario actualizado con éxito."], JSON_UNESCAPED_UNICODE);
        } else {
            echo json_encode(["success" => false, "mensaje" => "Error al actualizar usuario."], JSON_UNESCAPED_UNICODE);
        }
        break;

    case 'DELETE':
        $id = $_GET['id'] ?? null;
        if (!$id) {
            echo json_encode(["success" => false, "mensaje" => "ID no proporcionado."], JSON_UNESCAPED_UNICODE);
            exit();
        }
        $query = "DELETE FROM usuarios WHERE id = :id";
        $stmt = $conn->prepare($query);
        $stmt->bindParam(":id", $id);
        if ($stmt->execute()) {
            echo json_encode(["success" => true, "mensaje" => "Usuario eliminado."], JSON_UNESCAPED_UNICODE);
        } else {
            echo json_encode(["success" => false, "mensaje" => "Error al eliminar usuario."], JSON_UNESCAPED_UNICODE);
        }
        break;
}
?>
