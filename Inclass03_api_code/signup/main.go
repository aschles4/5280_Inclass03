package main

import (
	"context"
	"database/sql"
	"github.com/aws/aws-lambda-go/lambda"
	_ "github.com/go-sql-driver/mysql"
	"github.com/kelseyhightower/envconfig"
	"github.com/pkg/errors"
	"github.com/rs/zerolog"
	"os"
)

type SignupRequest struct {
	Email string `json:"email"`
	Password string `json:"password"`
	Fname string `json:"firstName"`
	Lname string `json:"lastName"`
	Age int64 `json:"age"`
	Weight float64 `json:"weight"`
	Address string `json:"address"`
}

type SignupResponse struct {
	UserID int64 `json:"userId"`
}

type Env struct {
	Connection string `required:"true" default:"" envconfig:"CONNECTION"`
}

type Handler struct {
	Env Env
	DB *sql.DB
	Stmts map[string]*sql.Stmt
	l zerolog.Logger
}


func (h *Handler) HandleRequest(ctx context.Context, req SignupRequest) (*SignupResponse, error) {
	//create user in database
	row, err := h.Stmts["create-user"].ExecContext(ctx, req.Email, req.Password, req.Fname, req.Lname, req.Age, req.Weight, req.Address)
	if err != nil {
		h.l.Info().Msg(err.Error())
		return nil, errors.Wrap(err, "failed to insert log")
	}

	_, err = row.RowsAffected()
	if err != nil {
		h.l.Info().Msg(err.Error())
		return nil, errors.Wrap(err, "zero rows inserted")
	}

	id, err := row.LastInsertId()
	if err != nil {
		h.l.Info().Msg(err.Error())
		return nil, errors.Wrap(err, "failed to get row id")
	}

	//return user id
	resp :=  SignupResponse{
		UserID: id,
	}

	return &resp, nil
}

func main() {

	l := zerolog.New(os.Stderr).With().Timestamp().Logger()

	var e Env
	err := envconfig.Process("", &e)
	if err != nil {
		l.Info().Msg(err.Error())
		l.Fatal().Msg("failed to parse envs")
	}


	db, err := sql.Open("mysql", e.Connection)
	if err != nil {
		l.Info().Msg(err.Error())
		l.Fatal().Msg("failed to connect to database")
		panic(err.Error())
	}

	defer db.Close()

	unprepared := map[string]string{
		"create-user": `
			INSERT INTO 
			users.users 
			(email, password, firstName, lastName, age, weight, address)
			VALUES (?,?,?,?,?,?,?);
		`,
	}

	stmts, err := PrepareStmts(db, unprepared)
	if err != nil {
		l.Info().Msg(err.Error())
		l.Fatal().Msg("could not prepare mysql statements")
	}

	h := Handler{
		Env: e,
		DB: db,
		Stmts: stmts,
		l: l,
	}

	lambda.Start(h.HandleRequest)
}

func PrepareStmts(db *sql.DB, unprepared map[string]string) (map[string]*sql.Stmt, error) {
	prepared := map[string]*sql.Stmt{}
	for k, v := range unprepared {
		s, err := db.Prepare(v)
		if err != nil {
			return nil, err
		}
		prepared[k] = s
	}
	return prepared, nil
}