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

type FindProfileRequest struct {
	Token string `json:"token"`
}

type FindProfileResponse struct {
	UserID int64 `json:"userID"`
	Email string `json:"email"`
	Fname string `json:"firstName"`
	Password string `json:"password"`
	Lname string `json:"lastName"`
	Age int64 `json:"age"`
	Weight float64 `json:"weight"`
	Address string `json:"address"`
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


func (h *Handler) HandleRequest(ctx context.Context, req FindProfileRequest) (*FindProfileResponse, error) {
	//check if token is valid
	row := h.Stmts["find-user-session-by-token"].QueryRowContext(ctx, req.Token)

	var userID int64
	err := row.Scan(
		&userID,
	)
	if err != nil {
		return nil, errors.Wrap(err, "session not active")
	}

	//Find user profile
	uRow := h.Stmts["find-user-information"].QueryRowContext(ctx, userID)

	var resp FindProfileResponse
	err = uRow.Scan(
		&resp.UserID,
		&resp.Email,
		&resp.Password,
		&resp.Fname,
		&resp.Lname,
		&resp.Age,
		&resp.Weight,
		&resp.Address,
	)
	if err != nil {
		return nil, errors.Wrap(err, "could not find user profile")
	}

	//Return user profile
	return &resp, nil
}

func main() {

	l := zerolog.New(os.Stderr).With().Timestamp().Logger()

	var e Env
	err := envconfig.Process("", &e)
	if err != nil {
		l.Fatal().Msg("failed to parse envs")
	}

	db, err := sql.Open("mysql", e.Connection)
	if err != nil {
		l.Fatal().Msg("failed to connect to database")
		panic(err.Error())
	}

	defer db.Close()

	unprepared := map[string]string{
		//Add time expires to this
		"find-user-session-by-token": `
			SELECT
				s.userId
			FROM users.sessions s
			WHERE s.token = ?
			AND s.active = 1
			LIMIT 1;
		`,
		"find-user-information": `
			SELECT
				u.id,
				u.email,
				u.password,
				u.firstName,
				u.lastName,
				u.age,
				u.weight,
				u.address
			FROM users.users u
			WHERE u.id = ?
			LIMIT 1;
		`,
	}

	stmts, err := PrepareStmts(db, unprepared)
	if err != nil {
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