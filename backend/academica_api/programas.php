<?php
require_once "db.php";

$method = $_SERVER['REQUEST_METHOD'] ?? 'GET';

switch ($method) {
    case 'GET':
        $query = "SELECT id, nombre, IFNULL(descripcion, '') AS descripcion FROM programas ORDER BY id DESC";
        $stmt = $conn->prepare($query);
        $stmt->execute();
        $programas = $stmt->fetchAll(PDO::FETCH_ASSOC);
        foreach ($programas as &$p) {
            $p['id'] = (string)$p['id'];
        }
        echo json_encode(["success" => true, "data" => $programas], JSON_UNESCAPED_UNICODE);
        break;

    case 'POST':
        $data = json_decode(file_get_contents("php://input"), true);
        if (empty($data['nombre'])) {
            echo json_encode(["success" => false, "mensaje" => "Nombre obligatorio."], JSON_UNESCAPED_UNICODE);
            exit();
        }
        $desc = $data['descripcion'] ?? '';
        $query = "INSERT INTO programas (nombre, descripcion) VALUES (:nombre, :descripcion)";
        $stmt = $conn->prepare($query);
        $stmt->bindParam(":nombre", $data['nombre']);
        $stmt->bindParam(":descripcion", $desc);

        if ($stmt->execute()) {
            echo json_encode(["success" => true, "mensaje" => "Programa registrado con éxito."], JSON_UNESCAPED_UNICODE);
        } else {
            echo json_encode(["success" => false, "mensaje" => "Error al registrar programa."], JSON_UNESCAPED_UNICODE);
        }
        break;

    case 'PUT':
        $data = json_decode(file_get_contents("php://input"), true);
        if (empty($data['id']) || empty($data['nombre'])) {
            echo json_encode(["success" => false, "mensaje" => "Datos incompletos."], JSON_UNESCAPED_UNICODE);
            exit();
        }
        $desc = $data['descripcion'] ?? '';
        $query = "UPDATE programas SET nombre = :nombre, descripcion = :descripcion WHERE id = :id";
        $stmt = $conn->prepare($query);
        $stmt->bindParam(":nombre", $data['nombre']);
        $stmt->bindParam(":descripcion", $desc);
        $stmt->bindParam(":id", $data['id']);

        if ($stmt->execute()) {
            echo json_encode(["success" => true, "mensaje" => "Programa actualizado con éxito."], JSON_UNESCAPED_UNICODE);
        } else {
            echo json_encode(["success" => false, "mensaje" => "Error al actualizar programa."], JSON_UNESCAPED_UNICODE);
        }
        break;

    case 'DELETE':
        $id = $_GET['id'] ?? null;
        if (!$id) {
            echo json_encode(["success" => false, "mensaje" => "ID no proporcionado."], JSON_UNESCAPED_UNICODE);
            exit();
        }
        $query = "DELETE FROM programas WHERE id = :id";
        $stmt = $conn->prepare($query);
        $stmt->bindParam(":id", $id);
        if ($stmt->execute()) {
            echo json_encode(["success" => true, "mensaje" => "Programa eliminado."], JSON_UNESCAPED_UNICODE);
        } else {
            echo json_encode(["success" => false, "mensaje" => "Error al eliminar programa."], JSON_UNESCAPED_UNICODE);
        }
        break;
}
?>
