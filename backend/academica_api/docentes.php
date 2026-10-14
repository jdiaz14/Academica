<?php
require_once "db.php";

$method = $_SERVER['REQUEST_METHOD'] ?? 'GET';

switch ($method) {
    case 'GET':
        $query = "SELECT dt.id,
                         u.nombre_completo AS docente,
                         p.nombre AS proyecto,
                         IFNULL(dt.fecha_asignacion, CURRENT_DATE) AS fecha_asignacion
                  FROM docentes_tutores dt
                  INNER JOIN usuarios u ON dt.docente_id = u.id
                  INNER JOIN proyectos p ON dt.proyecto_id = p.id
                  ORDER BY dt.id DESC";
        $stmt = $conn->prepare($query);
        $stmt->execute();
        $tutores = $stmt->fetchAll(PDO::FETCH_ASSOC);
        foreach ($tutores as &$t) {
            $t['id'] = (string)$t['id'];
        }
        echo json_encode(["success" => true, "data" => $tutores], JSON_UNESCAPED_UNICODE);
        break;

    case 'POST':
        $data = json_decode(file_get_contents("php://input"), true);
        if (empty($data['docente']) || empty($data['proyecto'])) {
            echo json_encode(["success" => false, "mensaje" => "Docente y proyecto obligatorios."], JSON_UNESCAPED_UNICODE);
            exit();
        }

        $qDoc = $conn->prepare("SELECT id FROM usuarios WHERE nombre_completo LIKE :nombre AND rol_id = 2 LIMIT 1");
        $likeDoc = "%" . trim($data['docente']) . "%";
        $qDoc->bindParam(":nombre", $likeDoc);
        $qDoc->execute();
        $docRow = $qDoc->fetch(PDO::FETCH_ASSOC);

        if (!$docRow) {
            $qDoc = $conn->prepare("SELECT id FROM usuarios WHERE rol_id = 2 LIMIT 1");
            $qDoc->execute();
            $docRow = $qDoc->fetch(PDO::FETCH_ASSOC);
        }

        $qProy = $conn->prepare("SELECT id FROM proyectos WHERE nombre LIKE :nombre LIMIT 1");
        $likeProy = "%" . trim($data['proyecto']) . "%";
        $qProy->bindParam(":nombre", $likeProy);
        $qProy->execute();
        $proyRow = $qProy->fetch(PDO::FETCH_ASSOC);

        if (!$proyRow) {
            $qProy = $conn->prepare("SELECT id FROM proyectos LIMIT 1");
            $qProy->execute();
            $proyRow = $qProy->fetch(PDO::FETCH_ASSOC);
        }

        if ($docRow && $proyRow) {
            $query = "INSERT INTO docentes_tutores (docente_id, proyecto_id, fecha_asignacion) VALUES (:docente_id, :proyecto_id, CURRENT_DATE)";
            $stmt = $conn->prepare($query);
            $stmt->bindParam(":docente_id", $docRow['id']);
            $stmt->bindParam(":proyecto_id", $proyRow['id']);

            if ($stmt->execute()) {
                echo json_encode(["success" => true, "mensaje" => "Asignación de tutor registrada."], JSON_UNESCAPED_UNICODE);
            } else {
                echo json_encode(["success" => false, "mensaje" => "Error al registrar asignación."], JSON_UNESCAPED_UNICODE);
            }
        } else {
            echo json_encode(["success" => false, "mensaje" => "No se encontró el docente o proyecto especificado."], JSON_UNESCAPED_UNICODE);
        }
        break;

    case 'PUT':
        $data = json_decode(file_get_contents("php://input"), true);
        if (empty($data['id']) || empty($data['docente']) || empty($data['proyecto'])) {
            echo json_encode(["success" => false, "mensaje" => "Datos incompletos."], JSON_UNESCAPED_UNICODE);
            exit();
        }

        $qDoc = $conn->prepare("SELECT id FROM usuarios WHERE nombre_completo LIKE :nombre AND rol_id = 2 LIMIT 1");
        $likeDoc = "%" . trim($data['docente']) . "%";
        $qDoc->bindParam(":nombre", $likeDoc);
        $qDoc->execute();
        $docRow = $qDoc->fetch(PDO::FETCH_ASSOC);

        if (!$docRow) {
            $qDoc = $conn->prepare("SELECT id FROM usuarios WHERE rol_id = 2 LIMIT 1");
            $qDoc->execute();
            $docRow = $qDoc->fetch(PDO::FETCH_ASSOC);
        }

        $qProy = $conn->prepare("SELECT id FROM proyectos WHERE nombre LIKE :nombre LIMIT 1");
        $likeProy = "%" . trim($data['proyecto']) . "%";
        $qProy->bindParam(":nombre", $likeProy);
        $qProy->execute();
        $proyRow = $qProy->fetch(PDO::FETCH_ASSOC);

        if (!$proyRow) {
            $qProy = $conn->prepare("SELECT id FROM proyectos LIMIT 1");
            $qProy->execute();
            $proyRow = $qProy->fetch(PDO::FETCH_ASSOC);
        }

        if ($docRow && $proyRow) {
            $query = "UPDATE docentes_tutores SET docente_id = :docente_id, proyecto_id = :proyecto_id WHERE id = :id";
            $stmt = $conn->prepare($query);
            $stmt->bindParam(":docente_id", $docRow['id']);
            $stmt->bindParam(":proyecto_id", $proyRow['id']);
            $stmt->bindParam(":id", $data['id']);

            if ($stmt->execute()) {
                echo json_encode(["success" => true, "mensaje" => "Asignación actualizada con éxito."], JSON_UNESCAPED_UNICODE);
            } else {
                echo json_encode(["success" => false, "mensaje" => "Error al actualizar asignación."], JSON_UNESCAPED_UNICODE);
            }
        } else {
            echo json_encode(["success" => false, "mensaje" => "No se encontró el docente o proyecto especificado."], JSON_UNESCAPED_UNICODE);
        }
        break;

    case 'DELETE':
        $id = $_GET['id'] ?? null;
        if (!$id) {
            echo json_encode(["success" => false, "mensaje" => "ID no proporcionado."], JSON_UNESCAPED_UNICODE);
            exit();
        }
        $query = "DELETE FROM docentes_tutores WHERE id = :id";
        $stmt = $conn->prepare($query);
        $stmt->bindParam(":id", $id);
        if ($stmt->execute()) {
            echo json_encode(["success" => true, "mensaje" => "Asignación eliminada."], JSON_UNESCAPED_UNICODE);
        } else {
            echo json_encode(["success" => false, "mensaje" => "Error al eliminar asignación."], JSON_UNESCAPED_UNICODE);
        }
        break;
}
?>
