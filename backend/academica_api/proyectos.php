<?php
require_once "db.php";

$method = $_SERVER['REQUEST_METHOD'] ?? 'GET';

switch ($method) {
    case 'GET':
        $query = "SELECT pr.id, pr.nombre, IFNULL(pr.descripcion, '') AS descripcion,
                         IFNULL(pr.fecha_registro, CURRENT_DATE) AS fecha_registro,
                         IFNULL(a.nombre, 'General') AS asignatura
                  FROM proyectos pr
                  LEFT JOIN asignaturas a ON pr.asignatura_id = a.id
                  ORDER BY pr.id DESC";
        $stmt = $conn->prepare($query);
        $stmt->execute();
        $proyectos = $stmt->fetchAll(PDO::FETCH_ASSOC);
        foreach ($proyectos as &$p) {
            $p['id'] = (string)$p['id'];
        }
        echo json_encode(["success" => true, "data" => $proyectos]);
        break;

    case 'POST':
        $data = json_decode(file_get_contents("php://input"), true);
        if (empty($data['nombre'])) {
            echo json_encode(["success" => false, "mensaje" => "El nombre del proyecto es obligatorio."]);
            exit();
        }
        $desc = $data['descripcion'] ?? '';
        $query = "INSERT INTO proyectos (nombre, descripcion, fecha_registro) VALUES (:nombre, :descripcion, CURRENT_DATE)";
        $stmt = $conn->prepare($query);
        $stmt->bindParam(":nombre", $data['nombre']);
        $stmt->bindParam(":descripcion", $desc);

        if ($stmt->execute()) {
            echo json_encode(["success" => true, "mensaje" => "Proyecto registrado con éxito."]);
        } else {
            echo json_encode(["success" => false, "mensaje" => "Error al registrar proyecto."]);
        }
        break;

    case 'PUT':
        $data = json_decode(file_get_contents("php://input"), true);
        if (empty($data['id']) || empty($data['nombre'])) {
            echo json_encode(["success" => false, "mensaje" => "Datos incompletos."]);
            exit();
        }
        $desc = $data['descripcion'] ?? '';
        $query = "UPDATE proyectos SET nombre = :nombre, descripcion = :descripcion WHERE id = :id";
        $stmt = $conn->prepare($query);
        $stmt->bindParam(":nombre", $data['nombre']);
        $stmt->bindParam(":descripcion", $desc);
        $stmt->bindParam(":id", $data['id']);

        if ($stmt->execute()) {
            echo json_encode(["success" => true, "mensaje" => "Proyecto actualizado con éxito."]);
        } else {
            echo json_encode(["success" => false, "mensaje" => "Error al actualizar proyecto."]);
        }
        break;

    case 'DELETE':
        $id = $_GET['id'] ?? null;
        if (!$id) {
            echo json_encode(["success" => false, "mensaje" => "ID no proporcionado."]);
            exit();
        }
        $query = "DELETE FROM proyectos WHERE id = :id";
        $stmt = $conn->prepare($query);
        $stmt->bindParam(":id", $id);
        if ($stmt->execute()) {
            echo json_encode(["success" => true, "mensaje" => "Proyecto eliminado."]);
        } else {
            echo json_encode(["success" => false, "mensaje" => "Error al eliminar proyecto."]);
        }
        break;
}
?>
