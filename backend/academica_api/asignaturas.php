<?php
require_once "db.php";

$method = $_SERVER['REQUEST_METHOD'] ?? 'GET';

switch ($method) {
    case 'GET':
        $query = "SELECT a.id, a.nombre, a.codigo, a.creditos, a.estado, IFNULL(p.nombre, 'General') AS programa
                  FROM asignaturas a
                  LEFT JOIN programas p ON a.programa_id = p.id
                  ORDER BY a.id DESC";
        $stmt = $conn->prepare($query);
        $stmt->execute();
        $asignaturas = $stmt->fetchAll(PDO::FETCH_ASSOC);
        foreach ($asignaturas as &$a) {
            $a['id'] = (string)$a['id'];
            $a['creditos'] = (string)$a['creditos'];
        }
        echo json_encode(["success" => true, "data" => $asignaturas], JSON_UNESCAPED_UNICODE);
        break;

    case 'POST':
        $data = json_decode(file_get_contents("php://input"), true);
        if (empty($data['nombre']) || empty($data['codigo'])) {
            echo json_encode(["success" => false, "mensaje" => "Nombre y código son obligatorios."], JSON_UNESCAPED_UNICODE);
            exit();
        }
        $creditos = $data['creditos'] ?? 3;
        $estado = $data['estado'] ?? 'Activa';
        $query = "INSERT INTO asignaturas (nombre, codigo, creditos, estado) VALUES (:nombre, :codigo, :creditos, :estado)";
        $stmt = $conn->prepare($query);
        $stmt->bindParam(":nombre", $data['nombre']);
        $stmt->bindParam(":codigo", $data['codigo']);
        $stmt->bindParam(":creditos", $creditos);
        $stmt->bindParam(":estado", $estado);

        if ($stmt->execute()) {
            echo json_encode(["success" => true, "mensaje" => "Asignatura registrada con éxito."], JSON_UNESCAPED_UNICODE);
        } else {
            echo json_encode(["success" => false, "mensaje" => "Error al registrar asignatura."], JSON_UNESCAPED_UNICODE);
        }
        break;

    case 'PUT':
        $data = json_decode(file_get_contents("php://input"), true);
        if (empty($data['id']) || empty($data['nombre']) || empty($data['codigo'])) {
            echo json_encode(["success" => false, "mensaje" => "Datos incompletos."], JSON_UNESCAPED_UNICODE);
            exit();
        }
        $creditos = $data['creditos'] ?? 3;
        $estado = $data['estado'] ?? 'Activa';
        $query = "UPDATE asignaturas SET nombre = :nombre, codigo = :codigo, creditos = :creditos, estado = :estado WHERE id = :id";
        $stmt = $conn->prepare($query);
        $stmt->bindParam(":nombre", $data['nombre']);
        $stmt->bindParam(":codigo", $data['codigo']);
        $stmt->bindParam(":creditos", $creditos);
        $stmt->bindParam(":estado", $estado);
        $stmt->bindParam(":id", $data['id']);

        if ($stmt->execute()) {
            echo json_encode(["success" => true, "mensaje" => "Asignatura actualizada con éxito."], JSON_UNESCAPED_UNICODE);
        } else {
            echo json_encode(["success" => false, "mensaje" => "Error al actualizar asignatura."], JSON_UNESCAPED_UNICODE);
        }
        break;

    case 'DELETE':
        $id = $_GET['id'] ?? null;
        if (!$id) {
            echo json_encode(["success" => false, "mensaje" => "ID no proporcionado."], JSON_UNESCAPED_UNICODE);
            exit();
        }
        $query = "DELETE FROM asignaturas WHERE id = :id";
        $stmt = $conn->prepare($query);
        $stmt->bindParam(":id", $id);
        if ($stmt->execute()) {
            echo json_encode(["success" => true, "mensaje" => "Asignatura eliminada."], JSON_UNESCAPED_UNICODE);
        } else {
            echo json_encode(["success" => false, "mensaje" => "Error al eliminar asignatura."], JSON_UNESCAPED_UNICODE);
        }
        break;
}
?>
