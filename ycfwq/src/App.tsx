import {
  BrowserRouter as Router,
  Routes,
  Route,
  Navigate,
} from "react-router-dom";
import Connections from "@/pages/Connections";
import Terminal from "@/pages/Terminal";
import Monitor from "@/pages/Monitor";

export default function App() {
  return (
    <Router>
      <Routes>
        <Route path="/" element={<Navigate to="/connections" replace />} />
        <Route path="/connections" element={<Connections />} />
        <Route path="/terminal/:connectionId" element={<Terminal />} />
        <Route path="/monitor/:connectionId" element={<Monitor />} />
        <Route path="*" element={<Navigate to="/connections" replace />} />
      </Routes>
    </Router>
  );
}
