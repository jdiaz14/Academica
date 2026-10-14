<?php
require_once "db.php";

$method = $_SERVER['REQUEST_METHOD'] ?? 'GET';

switch ($method) {
    case 'GET':
        $query = "SELECT e.id, e.nombre,
                         IFNULL(GROUP_CONCAT(u.nombre_completo SEPARATOR ', '), 'Sin integrantes') AS integrantes
                  FROM equipos e
                  LEFT JOIN integrantes_equipo ie ON e.id = ie.equipo_id
                  LEFT JOIN usuarios u ON ie.usuario_id = u.id
                  GROUP BY e.id, e.nombre
                  ORDER BY e.id DESC";
        $stmt = $conn->prepare($query);
        $stmt->execute();
        $equipos = $stmt->fetchAll(PDO::FETCH_ASSOC);
        foreach ($equipos as &$eq) {
            $eq['id'] = (string)$eq['id'];
        }
        echo json_encode(["success" => true, "data" => $equipos], JSON_UNESCAPED_UNICODE);
        break;

    case 'POST':
        $data = json_decode(file_get_contents("php://input"), true);
        if (empty($data['nombre'])) {
            echo json_encode(["success" => false, "mensaje" => "El nombre del equipo es obligatorio."], JSON_UNESCAPED_UNICODE);
            exit();
        }
        $query = "INSERT INTO equipos (nombre) VALUES (:nombre)";
        $stmt = $conn->prepare($query);
        $stmt->bindParam(":nombre", $data['nombre']);

        if ($stmt->execute()) {
            $equipo_id = $conn->lastInsertId();
            if (!empty($data['integrantes_texto'])) {
                $nombres = explode(",", $data['integrantes_texto']);
                foreach ($nombres as $n) {
                    $n = trim($n);
                    if (!empty($n)) {
                        $qUser = $conn->prepare("SELECT id FROM usuarios WHERE nombre_completo LIKE :nombre LIMIT 1");
                        $likeName = "%$n%";
                        $qUser->bindParam(":nombre", $likeName);
                        $qUser->execute();
                        $uRow = $qUser->fetch(PDO::FETCH_ASSOC);
                        if ($uRow) {
                            $qIns = $conn->prepare("INSERT INTO integrantes_equipo (equipo_id, usuario_id) VALUES (:eq, :us)");
                            $qIns->execute([':eq' => $equipo_id, ':us' => $uRow['id']]);
                        }
                    }
                }
            }
            echo json_encode(["success" => true, "mensaje" => "Equipo registrado exitosamente."], JSON_UNESCAPED_UNICODE);
        } else {
            echo json_encode(["success" => false, "mensaje" => "Error al registrar equipo."], JSON_UNESCAPED_UNICODE);
        }
        break;

    case 'PUT':
        $data = json_decode(file_get_contents("php://input"), true);
        if (empty($data['id']) || empty($data['nombre'])) {
            echo json_encode(["success" => false, "mensaje" => "Datos incompletos."], JSON_UNESCAPED_UNICODE);
            exit();
        }
        $query = "UPDATE equipos SET nombre = :nombre WHERE id = :id";
        $stmt = $conn->prepare($query);
        $stmt->bindParam(":nombre", $data['nombre']);
        $stmt->bindParam(":id", $data['id']);

        if ($stmt->execute()) {
            if (isset($data['integrantes_texto'])) {
                $conn->prepare("DELETE FROM integrantes_equipo WHERE equipo_id = :eq")->execute([':eq' => $data['id']]);
                $nombres = explode(",", $data['integrantes_texto']);
                foreach ($nombres as $n) {
                    $n = trim($n);
                    if (!empty($n)) {
                        $qUser = $conn->prepare("SELECT id FROM usuarios WHERE nombre_completo LIKE :nombre LIMIT 1");
                        $likeName = "%$n%";
                        $qUser->bindParam(":nombre", $likeName);
                        $qUser->execute();
                        $uRow = $qUser->fetch(PDO::FETCH_ASSOC);
                        if ($uRow) {
                            $qIns = $conn->prepare("INSERT INTO integrantes_equipo (equipo_id, usuario_id) VALUES (:eq, :us)");
                            $qIns->execute([':eq' => $data['id'], ':us' => $uRow['id']]);
                        }
                    }
                }
            }
            echo json_encode(["success" => true, "mensaje" => "Equipo actualizado con éxito."], JSON_UNESCAPED_UNICODE);
        } else {
            echo json_encode(["success" => false, "mensaje" => "Error al actualizar equipo."], JSON_UNESCAPED_UNICODE);
        }
        break;

    case 'DELETE':
        $id = $_GET['id'] ?? null;
        if (!$id) {
            echo json_encode(["success" => false, "mensaje" => "ID no proporcionado."], JSON_UNESCAPED_UNICODE);
            exit();
        }
        $query = "DELETE FROM equipos WHERE id = :id";
        $stmt = $conn->prepare($query);
        $stmt->bindParam(":id", $id);
        if ($stmt->execute()) {
            echo json_encode(["success" => true, "mensaje" => "Equipo eliminado."], JSON_UNESCAPED_UNICODE);
        } else {
            echo json_encode(["success" => false, "mensaje" => "Error al eliminar equipo."], JSON_UNESCAPED_UNICODE);
        }
        break;
}
?>
