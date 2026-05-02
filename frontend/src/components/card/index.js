import "./index.css";

const Card = ({ data, columns }) => {
  return (
    <div className="card">
      {columns.map((col, index) => (        
        <div key={index} className="card-field">
          <div className="header">{col.headerName}</div>
          {col.cellRenderer ? (
            col.cellRenderer({ data })
          ) : (
            <span className="data">{data[col.field]}</span>
          )}
        </div>
      ))}
    </div>
  );
};

export default Card;
